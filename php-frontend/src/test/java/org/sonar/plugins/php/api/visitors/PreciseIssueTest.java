/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.plugins.php.api.visitors;

import java.util.Collections;
import org.junit.Test;
import org.sonar.php.symbols.LocationInFileImpl;
import org.sonar.php.symbols.UnknownLocationInFile;
import org.sonar.php.tree.impl.lexical.InternalSyntaxToken;
import org.sonar.php.utils.DummyCheck;
import org.sonar.plugins.php.api.tree.Tree;

import static org.assertj.core.api.Assertions.assertThat;

public class PreciseIssueTest {

  private static final PHPCheck CHECK = new DummyCheck();
  private static final IssueLocation PRIMARY_LOCATION = new IssueLocation(createToken(42, 5, "Hello"), "Test message");

  @Test
  public void test() throws Exception {
    PreciseIssue preciseIssue = new PreciseIssue(CHECK, PRIMARY_LOCATION);

    assertThat(preciseIssue.check()).isEqualTo(CHECK);
    assertThat(preciseIssue.cost()).isNull();
    assertThat(preciseIssue.primaryLocation().message()).isEqualTo("Test message");
    assertThat(preciseIssue.primaryLocation().startLine()).isEqualTo(42);
    assertThat(preciseIssue.primaryLocation().startLineOffset()).isEqualTo(5);
    assertThat(preciseIssue.secondaryLocations()).isEmpty();
  }

  @Test
  public void with_cost() throws Exception {
    PreciseIssue preciseIssue = new PreciseIssue(CHECK, PRIMARY_LOCATION).cost(5);
    assertThat(preciseIssue.cost()).isEqualTo(5);
  }

  @Test
  public void with_secondary() throws Exception {
    PreciseIssue preciseIssue = new PreciseIssue(CHECK, PRIMARY_LOCATION);
    preciseIssue.secondary(createToken(142, 0, "someValue"), "Secondary message");
    preciseIssue.secondary(createToken(242, 0, "someValue"), null);
    preciseIssue.secondary(createToken(342, 0, "someValue"), createToken(352, 0, "someValue"), null);

    assertThat(preciseIssue.secondaryLocations()).hasSize(3);
    assertThat(preciseIssue.secondaryLocations().get(0).message()).isEqualTo("Secondary message");
    assertThat(preciseIssue.secondaryLocations().get(1).message()).isNull();

    assertThat(preciseIssue.secondaryLocations().get(0).startLine()).isEqualTo(142);
    assertThat(preciseIssue.secondaryLocations().get(0).endLine()).isEqualTo(142);
    assertThat(preciseIssue.secondaryLocations().get(1).startLine()).isEqualTo(242);
    assertThat(preciseIssue.secondaryLocations().get(1).endLine()).isEqualTo(242);
    assertThat(preciseIssue.secondaryLocations().get(2).startLine()).isEqualTo(342);
    assertThat(preciseIssue.secondaryLocations().get(2).endLine()).isEqualTo(352);
  }

  @Test
  public void with_secondary_in_different_file() {
    PreciseIssue preciseIssue = new PreciseIssue(CHECK, PRIMARY_LOCATION);
    LocationInFileImpl locationInFile = new LocationInFileImpl("dir1/file1.php", 1, 2, 3, 4);
    preciseIssue.secondary(locationInFile, "Secondary message");
    assertThat(preciseIssue.secondaryLocations()).hasSize(1);
    IssueLocation secondary = preciseIssue.secondaryLocations().get(0);
    assertThat(secondary.message()).isEqualTo("Secondary message");
    assertThat(secondary.startLine()).isEqualTo(1);
    assertThat(secondary.startLineOffset()).isEqualTo(2);
    assertThat(secondary.endLine()).isEqualTo(3);
    assertThat(secondary.endLineOffset()).isEqualTo(4);
    assertThat(secondary.filePath()).isEqualTo("dir1/file1.php");
  }

  @Test
  public void secondary_on_unknown_location() {
    PreciseIssue preciseIssue = new PreciseIssue(CHECK, PRIMARY_LOCATION);
    preciseIssue.secondary(UnknownLocationInFile.UNKNOWN_LOCATION, "Secondary message");
    assertThat(preciseIssue.secondaryLocations()).isEmpty();
  }

  private static Tree createToken(int line, int column, String tokenValue) {
    return new InternalSyntaxToken(line, column, tokenValue, Collections.emptyList(), 0, false);
  }

}
