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


/** 
 * A byte transform is an operation that takes an array of bytes as input and
 * turns it into another array of bytes of the same size.
 */
public interface ByteTransform
{
    /**
     * Applies the transform on the provided array starting at the provided
     * index. Returns the transformed array of bytes.
     * @param block The array of bytes to perform the transform on
     * @param idx The starting index in the byte array
     * @return The final transformed array of bytes
     */
    public byte[] forward(byte[] block, int idx);
    
    /** Applies the inverse transform on the provided array of bytes starting 
     * at the provided index. Return the original array of bytes.
     * @param block The transformed array of bytes to return to the original
     * @param idx The starting index in the transformed array of bytes
     * @return The final original array of bytes before the transform
     */
    public byte[] inverse(byte[] block, int idx);
}

