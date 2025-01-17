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
import org.sonar.plugins.php.api.tree.Tree.Kind;

import static org.sonar.php.utils.Assertions.assertThat;

public class ExpandableStringLiteralTest {

  @Test
  public void test() {
    assertThat(Kind.EXPANDABLE_STRING_LITERAL)
      .matches("\"$var\"")
      .matches("\"str $var\"")
      .matches("\"$var $var\"")
      .matches("\"$var str\"")
      .matches("\"no escape for `backtick` \"")
      .matches("\"$var str $var\"");
  }

  @Test
  public void executionOperator() {
    assertThat(Kind.EXECUTION_OPERATOR)
      .matches("`$var`")
      .matches("`without expression`")
      .matches("`str $var`")
      .matches("`$var $var`")
      .matches("`$var str`")
      .matches("`no escape for quotes \" '`")
      .matches("`$var str $var`");
  }

  @Test
  public void test_real_life() {
    assertThat(Kind.EXPANDABLE_STRING_LITERAL)
      .matches("\"{$var[\"foo\"]}\"");
  }

}
