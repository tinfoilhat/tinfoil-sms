/**
 *********************************** NOTICE ***********************************
 *
 * This is a derived work originally written by Frederic Langlet under the
 * Apache License, Version 2.0. The original work's copyright notice and
 * authorship are described below, for more information about the original
 * work see the included NOTICE file.
 *
 *********************************** NOTICE ***********************************
 */

/** 
 * Copyright (C) 2012 Tinfoilhat
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http: *www.gnu.org/licenses/>.
 */

/**
 * Copyright 2011, 2012 Frederic Langlet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License at
 * 
 *                 http: *www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public final class IndexedIntArray
{
    public int[] array;
    public int index;
    
    
    public IndexedIntArray(int[] array, int idx)
    {
        if (array == null)
           throw new NullPointerException("The array cannot be null");
        
        this.array = array;
        this.index = idx;
    }
    
    
    @Override
    public boolean equals(Object o)
    {
        try
        {
            if (o == null)
               return false;

            if (this == o)
               return true;

            IndexedIntArray iba = (IndexedIntArray) o;
            return ((this.array == iba.array) && (this.index == iba.index));
        }
        catch (ClassCastException e)
        {
            return false;
        }
    }
        

    @Override
    public int hashCode()
    {
       // Non constant !
       return this.index + ((this.array == null) ? 0 :(17 * this.array.hashCode()));
    }


    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(100);
        builder.append("[");
        builder.append(String.valueOf(this.array));
        builder.append(","); 
        builder.append(this.index); 
        builder.append("]"); 
        return builder.toString();
    }
}
