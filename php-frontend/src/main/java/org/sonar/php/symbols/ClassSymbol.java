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
package org.sonar.php.symbols;

import java.util.List;
import java.util.Optional;
import org.sonar.plugins.php.api.symbols.QualifiedName;
import org.sonar.plugins.php.api.visitors.LocationInFile;

public interface ClassSymbol extends Symbol {

  LocationInFile location();

  QualifiedName qualifiedName();

  Optional<ClassSymbol> superClass();

  List<ClassSymbol> implementedInterfaces();

  Trilean isOrSubClassOf(QualifiedName qualifiedName);

  Trilean isSubTypeOf(QualifiedName... typeName);

  List<MethodSymbol> declaredMethods();

  MethodSymbol getDeclaredMethod(String name);

  boolean is(Kind kind);

  enum Kind {
    NORMAL,
    ABSTRACT,
    INTERFACE
  }
}
