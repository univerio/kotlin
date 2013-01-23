/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jet;

import org.jetbrains.jet.rt.annotation.AssertInvisibleInResolver;

@AssertInvisibleInResolver
public class ByteSequence implements NumberSequence<Byte> {
    private final byte start;
    private final byte end;
    private final int increment;

    public ByteSequence(byte start, byte end, int increment) {
        if (increment == 0) {
            throw new IllegalArgumentException("Increment must be non-zero: " + increment);
        }
        this.start = start;
        this.end = end;
        this.increment = increment;
    }

    @Override
    public Byte getStart() {
        return start;
    }

    @Override
    public Byte getEnd() {
        return end;
    }

    @Override
    public Integer getIncrement() {
        return increment;
    }

    @Override
    public ByteIterator iterator() {
        return new ByteSequenceIterator(start, end, increment);
    }
}