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
package org.sonar.php.parser.expression;

import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;

import static org.sonar.php.utils.Assertions.assertThat;

public class ListAssignmentExpressionTest {

  @Test
  public void test() {
    assertThat(PHPLexicalGrammar.LIST_EXPRESSION_ASSIGNMENT)
      .matches("list ($a) = array(1)")
      .matches("list($a, &$b) = $array")
      .matches("list(&$a, $b,, list(&$c, $d)) = $array")
      .matches("list(&$one, list($two, &$three)) = $a")
      .matches("list(&$one, [$two, &$three]) = $a")
      .notMatches("$array = [1, 2]");
  }
}
