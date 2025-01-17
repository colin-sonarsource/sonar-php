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
package org.sonar.php.tree.impl.expression;

import org.junit.Test;
import org.sonar.php.PHPTreeModelTest;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ArrayPairTree;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrayPairTreeTest extends PHPTreeModelTest {

  @Test
  public void value() throws Exception {
    ArrayPairTree tree = parse("$val", Kind.ARRAY_PAIR);

    assertThat(tree.is(Kind.ARRAY_PAIR)).isTrue();

    assertThat(tree.key()).isNull();
    assertThat(tree.doubleArrowToken()).isNull();
    assertThat(expressionToString(tree.value())).isEqualTo("$val");
  }

  @Test
  public void key_value() throws Exception {
    ArrayPairTree tree = parse("$key => $val", Kind.ARRAY_PAIR);

    assertThat(tree.is(Kind.ARRAY_PAIR)).isTrue();

    assertThat(expressionToString(tree.key())).isEqualTo("$key");
    assertThat(tree.doubleArrowToken().text()).isEqualTo("=>");
    assertThat(expressionToString(tree.value())).isEqualTo("$val");
  }

  @Test
  public void spread_operator() throws Exception {
    ArrayPairTree tree = parse("...$val", Kind.ARRAY_PAIR);
    assertThat(tree.ellipsisToken().text()).isEqualTo("...");
    assertThat(expressionToString(tree.value())).isEqualTo("$val");
  }

}
