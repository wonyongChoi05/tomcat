/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jakarta.el;

import java.util.Objects;

public class CompositeELResolver extends ELResolver {

    private static final Class<?> SCOPED_ATTRIBUTE_EL_RESOLVER;
    static {
        Class<?> clazz = null;
        try {
            clazz = Class.forName("jakarta.servlet.jsp.el.ScopedAttributeELResolver");
        } catch (ClassNotFoundException e) {
            // Ignore. This is expected if using the EL stand-alone
        }
        SCOPED_ATTRIBUTE_EL_RESOLVER = clazz;
    }

    private int size;

    private ELResolver[] resolvers;

    public CompositeELResolver() {
        this.size = 0;
        this.resolvers = new ELResolver[8];
    }

    public void add(ELResolver elResolver) {
        Objects.requireNonNull(elResolver);

        if (this.size >= this.resolvers.length) {
            ELResolver[] nr = new ELResolver[this.size * 2];
            System.arraycopy(this.resolvers, 0, nr, 0, this.size);
            this.resolvers = nr;
        }
        this.resolvers[this.size++] = elResolver;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        context.setPropertyResolved(false);
        int sz = this.size;
        for (int i = 0; i < sz; i++) {
            Object result = this.resolvers[i].getValue(context, base, property);
            if (context.isPropertyResolved()) {
                return result;
            }
        }
        return null;
    }

    /**
     * @since EL 2.2
     */
    @Override
    public Object invoke(ELContext context, Object base, Object method,
            Class<?>[] paramTypes, Object[] params) {
        context.setPropertyResolved(false);
        int sz = this.size;
        for (int i = 0; i < sz; i++) {
            Object obj = this.resolvers[i].invoke(context, base, method, paramTypes, params);
            if (context.isPropertyResolved()) {
                return obj;
            }
        }
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        context.setPropertyResolved(false);
        int sz = this.size;
        for (int i = 0; i < sz; i++) {
            Class<?> type = this.resolvers[i].getType(context, base, property);
            if (context.isPropertyResolved()) {
                if (SCOPED_ATTRIBUTE_EL_RESOLVER != null &&
                        SCOPED_ATTRIBUTE_EL_RESOLVER.isAssignableFrom(resolvers[i].getClass())) {
                    // Special case since
                    // jakarta.servlet.jsp.el.ScopedAttributeELResolver will
                    // always return Object.class for type
                    Object value = resolvers[i].getValue(context, base, property);
                    if (value != null) {
                        return value.getClass();
                    }
                }
                return type;
            }
        }
        return null;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        context.setPropertyResolved(false);
        int sz = this.size;
        for (int i = 0; i < sz; i++) {
            this.resolvers[i].setValue(context, base, property, value);
            if (context.isPropertyResolved()) {
                return;
            }
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        context.setPropertyResolved(false);
        int sz = this.size;
        for (int i = 0; i < sz; i++) {
            boolean readOnly = this.resolvers[i].isReadOnly(context, base, property);
            if (context.isPropertyResolved()) {
                return readOnly;
            }
        }
        return false;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        Class<?> commonType = null;
        int sz = this.size;
        for (int i = 0; i < sz; i++) {
            Class<?> type = this.resolvers[i].getCommonPropertyType(context, base);
            if (type != null && (commonType == null || commonType.isAssignableFrom(type))) {
                commonType = type;
            }
        }
        return commonType;
    }

    @Override
    public <T> T convertToType(ELContext context, Object obj, Class<T> type) {
        context.setPropertyResolved(false);
        int sz = this.size;
        for (int i = 0; i < sz; i++) {
            T result = this.resolvers[i].convertToType(context, obj, type);
            if (context.isPropertyResolved()) {
                return result;
            }
        }
        return null;
    }
}
