/******************************************************************************
 *    This file is part of Exceer                                             *
 *                                                                            *
 *    Copyright (C) 2015  Andreas Grapentin                                   *
 *                                                                            *
 *    This program is free software: you can redistribute it and/or modify    *
 *    it under the terms of the GNU General Public License as published by    *
 *    the Free Software Foundation, either version 3 of the License, or       *
 *    (at your option) any later version.                                     *
 *                                                                            *
 *    This program is distributed in the hope that it will be useful,         *
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of          *
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           *
 *    GNU General Public License for more details.                            *
 *                                                                            *
 *    You should have received a copy of the GNU General Public License       *
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.   *
 ******************************************************************************/

package org.grapentin.apps.exceer.helpers;

import org.grapentin.apps.exceer.MainActivity;
import org.grapentin.apps.exceer.managers.ContextManager;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;

import dalvik.system.DexFile;

public class Reflection
{

  public static ArrayList<Class> getSubclassesOf (Class base)
    {
      ArrayList<Class> derived = new ArrayList<>();

      try
        {
          DexFile df = new DexFile(ContextManager.get().getPackageCodePath());
          for (Enumeration<String> i = df.entries(); i.hasMoreElements(); )
            {
              String s = i.nextElement();
              if (s.startsWith(MainActivity.class.getPackage().getName()) && Class.forName(s).getSuperclass() == base)
                {
                  derived.add(Class.forName(s));
                  derived.addAll(getSubclassesOf(Class.forName(s)));
                }
            }
        }
      catch (Exception e)
        {
          throw new Error(e);
        }

      return derived;
    }

  public static ArrayList<Object> getDeclaredFieldsOfType (Class c, Type t)
    {
      ArrayList<Object> out = new ArrayList<>();
      try
        {
          Object o = c.newInstance();

          for (Field f : c.getDeclaredFields())
            if (f.getType() == t)
              {
                f.setAccessible(true);
                out.add(f.get(o));
              }

          if (c.getSuperclass() != Object.class)
            out.addAll(getDeclaredFieldsOfType(c.getSuperclass(), t, o));

          return out;
        }
      catch (Exception e)
        {
          throw new Error(e);
        }
    }

  public static ArrayList<Object> getDeclaredFieldsOfType (Class c, Type t, Object o)
    {
      ArrayList<Object> out = new ArrayList<>();
      try
        {
          for (Field f : c.getDeclaredFields())
            if (f.getType() == t)
              {
                f.setAccessible(true);
                out.add(f.get(o));
              }

          if (c.getSuperclass() != Object.class)
            out.addAll(getDeclaredFieldsOfType(c.getSuperclass(), t, o));

          return out;
        }
      catch (Exception e)
        {
          throw new Error(e);
        }
    }

}
