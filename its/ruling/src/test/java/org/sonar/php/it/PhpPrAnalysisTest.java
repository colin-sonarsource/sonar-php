/*
 * SonarQube PHP Plugin
 * Copyright (C) 2014-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.it;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.BuildResult;
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.container.Edition;
import com.sonar.orchestrator.locator.FileLocation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class PhpPrAnalysisTest {

  @ClassRule
  public static final Orchestrator ORCHESTRATOR = RulingHelper.getOrchestrator(Edition.DEVELOPER);

  private static final String PROJECT_KEY = "prAnalysis";

  private static final String INCREMENTAL_ANALYSIS_PROFILE = "incrementalPrAnalysis";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private final String scenario;
  private final int expectedTotalFiles;
  private final int expectedSkipped;
  private final List<String> deletedFiles;

  public PhpPrAnalysisTest(String scenario, int expectedTotalFiles, int expectedSkipped, List<String> deletedFiles) {
    this.scenario = scenario;
    this.expectedTotalFiles = expectedTotalFiles;
    this.expectedSkipped = expectedSkipped;
    this.deletedFiles = deletedFiles;
  }

  @BeforeClass
  public static void prepare_quality_profile() throws IOException {
    ORCHESTRATOR.getServer().provisionProject(PROJECT_KEY, PROJECT_KEY);

    // Create and load quality profile
    String profile = profile(INCREMENTAL_ANALYSIS_PROFILE, List.of("S1172", "S1808"));
    loadProfile(ORCHESTRATOR, profile);
    ORCHESTRATOR.getServer().associateProjectToQualityProfile(PROJECT_KEY, "php", INCREMENTAL_ANALYSIS_PROFILE);
  }

  @Parameterized.Parameters(name = "{index}: {0}")
  public static Collection<Object[]> data() {
    return List.of(new Object[][] {
      // {<scenario>, <total files>, <skipped>, <deleted>}
      {"added", 3, 2, Collections.emptyList()},
      {"changed", 2, 1, Collections.emptyList()},
      {"deleted", 1, 1, List.of("AbstractController.php")}
    });
  }

  @Test
  public void pr_analysis_logs() throws IOException {
    File tempDirectory = temporaryFolder.newFolder();
    File litsDifferencesFile = FileLocation.of("target/differences").getFile();

    // Analyze base commit
    analyzeAndAssertBaseCommit(tempDirectory, litsDifferencesFile);

    // Analyze the changed branch
    setUpChanges(tempDirectory, scenario);
    SonarScanner build = RulingHelper.prepareScanner(tempDirectory, PROJECT_KEY, "expected_pr_analysis/" + scenario, litsDifferencesFile)
      .setProperty("sonar.pullrequest.key", "1")
      .setProperty("sonar.pullrequest.branch", "incremental");

    BuildResult result = ORCHESTRATOR.executeBuild(build);
    assertPrAnalysisLogs(result);
  }

  @Test
  public void pr_analysis_issues() throws IOException {
    File tempDirectory = temporaryFolder.newFolder();
    File litsDifferencesFile = FileLocation.of("target/differences").getFile();

    // Analyze base commit
    analyzeAndAssertBaseCommit(tempDirectory, litsDifferencesFile);

    // Analyze the changed branch

    // By default, when performing branch analysis, the incremental analysis is disabled.
    // Still, while testing, we want to run branch analysis, to take full advantage of LITS by comparing the total issues that are raised
    // (if we set up a PR Analysis, LITS will fail comparing all the expected issues).
    // Thus, in the test we perform branch analysis, and we manually enable incremental analysis for testing purposes.
    setUpChanges(tempDirectory, scenario);
    SonarScanner build = RulingHelper.prepareScanner(tempDirectory, PROJECT_KEY, "expected_pr_analysis/" + scenario, litsDifferencesFile)
      .setProperty("sonar.php.skipUnchanged", "true");

    BuildResult result = ORCHESTRATOR.executeBuild(build);

    // Check expected issues
    String litsDifferences = new String(Files.readAllBytes(litsDifferencesFile.toPath()), UTF_8);
    assertThat(litsDifferences).isEmpty();
    assertPrAnalysisLogs(result);
  }

  private void analyzeAndAssertBaseCommit(File tempFile, File litsDifferencesFile) throws IOException {
    FileUtils.copyDirectory(new File("../sources_pr_analysis", "base"), tempFile);

    SonarScanner build = RulingHelper.prepareScanner(tempFile, PROJECT_KEY, "expected_pr_analysis/base", litsDifferencesFile);
    ORCHESTRATOR.executeBuild(build);

    String litsDifferences = new String(Files.readAllBytes(litsDifferencesFile.toPath()), UTF_8);
    assertThat(litsDifferences).isEmpty();
  }

  private void assertPrAnalysisLogs(BuildResult result) {
    String expectedUsedSymbolsFromCache = String.format("Cached information of global symbols will be used for %d out of %d files. " +
        "Global symbols were recomputed for the remaining files.",
      expectedSkipped, expectedTotalFiles);

    String expectedRegularAnalysisLog = String.format("The PHP analyzer was able to leverage cached data from previous analyses for " +
        "%d out of %d files. These files were not parsed.",
      expectedSkipped, expectedTotalFiles);

    assertThat(result.getLogs())
      .contains(expectedUsedSymbolsFromCache)
      .contains(expectedRegularAnalysisLog);
  }

  private void setUpChanges(File tempDirectory, String scenario) throws IOException {
    Arrays.stream(tempDirectory.listFiles(f -> deletedFiles.contains(f.getName()))).forEach(File::delete);
    FileUtils.copyDirectory(new File("../sources_pr_analysis", scenario), tempDirectory);
  }

  private static String profile(String name,  List<String> ruleKeys) {
    StringBuilder sb = new StringBuilder()
      .append("<profile>")
      .append("<name>").append(name).append("</name>")
      .append("<language>").append("php").append("</language>")
      .append("<rules>");
    ruleKeys.forEach(ruleKey -> {
      sb.append("<rule>")
        .append("<repositoryKey>").append("php").append("</repositoryKey>")
        .append("<key>").append(ruleKey).append("</key>")
        .append("<priority>INFO</priority>")
        .append("</rule>");
    });

    return sb
      .append("</rules>")
      .append("</profile>")
      .toString();
  }

  private static void loadProfile(Orchestrator orchestrator, String profile) throws IOException {
    File file = File.createTempFile("profile", ".xml");
    Files.write(file.toPath(), profile.getBytes());
    orchestrator.getServer().restoreProfile(FileLocation.of(file));
    file.delete();
  }
}
