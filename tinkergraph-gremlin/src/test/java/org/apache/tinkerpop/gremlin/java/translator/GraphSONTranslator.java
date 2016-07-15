/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.tinkerpop.gremlin.java.translator;

import org.apache.tinkerpop.gremlin.process.traversal.Bytecode;
import org.apache.tinkerpop.gremlin.process.traversal.Translator;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.verification.VerificationException;
import org.apache.tinkerpop.gremlin.process.traversal.util.BytecodeHelper;
import org.apache.tinkerpop.gremlin.process.traversal.util.EmptyTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.util.JavaTranslator;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONReader;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
final class GraphSONTranslator<S extends TraversalSource, T extends Traversal.Admin<?, ?>> implements Translator.StepTranslator<S, T> {

    private final JavaTranslator<S, T> wrappedTranslator;
    private final GraphSONWriter writer = GraphSONWriter.build().create();
    private final GraphSONReader reader = GraphSONReader.build().create();

    public GraphSONTranslator(final JavaTranslator<S, T> wrappedTranslator) {
        this.wrappedTranslator = wrappedTranslator;
    }

    @Override
    public S getTraversalSource() {
        return this.wrappedTranslator.getTraversalSource();
    }

    @Override
    public Class getAnonymousTraversal() {
        return this.wrappedTranslator.getAnonymousTraversal();
    }

    @Override
    public T translate(final Bytecode bytecode) {
        try {
            for (final Bytecode.Instruction instruction : bytecode.getStepInstructions()) {
                for (final Object argument : instruction.getArguments()) {
                    if (argument.toString().contains("$"))
                        throw new VerificationException("Lambdas are currently not supported: " + bytecode, EmptyTraversal.instance());
                }
            }
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            this.writer.writeObject(outputStream, BytecodeHelper.filterInstructions(bytecode,
                    instruction -> !Arrays.asList("withTranslator", "withStrategies").contains(instruction.getOperator())));
            // System.out.println(new String(outputStream.toByteArray()));
            return this.wrappedTranslator.translate(this.reader.readObject(new ByteArrayInputStream(outputStream.toByteArray()), Bytecode.class));
        } catch (final Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public String getTargetLanguage() {
        return this.wrappedTranslator.getTargetLanguage();
    }
}