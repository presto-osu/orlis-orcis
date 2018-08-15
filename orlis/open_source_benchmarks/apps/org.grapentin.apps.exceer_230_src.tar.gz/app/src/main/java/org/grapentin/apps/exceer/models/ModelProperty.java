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

package org.grapentin.apps.exceer.models;

import org.grapentin.apps.exceer.helpers.XmlNode;

import java.util.ArrayList;

public class ModelProperty extends BaseModel
{

  protected final static String TABLE_NAME = "properties";

  public Column key = new Column("key");
  public Column value = new Column("value");

  public static ModelProperty fromXml (XmlNode root)
    {
      ModelProperty m = new ModelProperty();

      m.key.set(root.getAttribute("name"));
      m.value.set(root.getValue());

      return m;
    }

  public static ModelProperty get (long id)
    {
      return (ModelProperty)BaseModel.get(ModelProperty.class, id);
    }

  public static ArrayList<ModelProperty> getAll ()
    {
      ArrayList<ModelProperty> out = new ArrayList<>();

      for (long id : BaseModel.getAllIds(ModelProperty.class))
        out.add(get(id));

      return out;
    }

}
