/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.authorization.test.helper;

import org.mockito.ArgumentMatcher;
import org.mockito.internal.matchers.VarargMatcher;

/**
 * Convenience class for matching varargs.
 * 
 */
public class ObjectVarArgsMatcher extends ArgumentMatcher<Object> implements
        VarargMatcher {

    private static final long serialVersionUID = 1L;
    private final Object[] expectedValues;

    public ObjectVarArgsMatcher(Object... expectedValues) {
        this.expectedValues = expectedValues;
    }

    @Override
    public boolean matches(Object varargArgument) {
        boolean equals = true;

        if (!varargArgument.getClass().isArray()) {
            return expectedValues[0].equals(varargArgument);
        } else {
            Object[] args = (Object[]) varargArgument;
            if (args.length != expectedValues.length) {
                return false;
            } else {
                for (int i = 0; i < args.length; i++) {
                    equals &= args[i].equals(expectedValues[i]);
                }
            }
        }

        return equals;
    }

}
