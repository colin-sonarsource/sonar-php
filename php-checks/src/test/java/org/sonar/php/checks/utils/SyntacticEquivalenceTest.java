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
package org.sonar.php.checks.utils;

import com.sonar.sslr.api.typed.ActionParser;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonarsource.analyzer.commons.checks.coverage.UtilityClass;

import static org.assertj.core.api.Assertions.assertThat;

public class SyntacticEquivalenceTest {

  private ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.TOP_STATEMENT);

  @Test
  public void utility_class() throws Exception {
    UtilityClass.assertGoodPractice(SyntacticEquivalence.class);
  }

  @Test
  public void areSyntacticallyEquivalent() throws Exception {
    assertThat(areSyntacticallyEquivalent(null, null)).isTrue();
    assertThat(areSyntacticallyEquivalent(null, "1;")).isFalse();
    assertThat(areSyntacticallyEquivalent("$x;", null)).isFalse();
    assertThat(areSyntacticallyEquivalent("$x;", "1;")).isFalse();
    assertThat(areSyntacticallyEquivalent("$x;", "$x;")).isTrue();
    assertThat(areSyntacticallyEquivalent("$x;", "$y;")).isFalse();
    assertThat(areSyntacticallyEquivalent("switch ($a) {case 1:}", "switch ($a) {case 1:}")).isTrue();
    assertThat(areSyntacticallyEquivalent("switch ($a) {case 1:}", "switch ($a) {case 1: case2:}")).isFalse();
    assertThat(areSyntacticallyEquivalent("switch ($a) {case 1: case2:}", "switch ($a) {case 1:}")).isFalse();
  }

  private boolean areSyntacticallyEquivalent(@Nullable String toParse1, @Nullable String toParse2) {
    return SyntacticEquivalence.areSyntacticallyEquivalent(parse(toParse1), parse(toParse2));
  }

  @CheckForNull
  private Tree parse(@Nullable String toParse) {
    return toParse == null ? null : parser.parse(toParse);
  }

}
