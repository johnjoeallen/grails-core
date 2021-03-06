/*
 * Copyright 2014 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.cli.profile

/**
 * An interface that represents a command to be executed by the Grails command line. Commands are by default global,
 * however a command can be made specific to a particular {@link Profile} by implementation the {@link ProfileCommand} interface.
 *
 * @author Graeme Rocher
 * @since 3.0
 */
interface Command {
    /**
     * @return The name of the command
     */
    String getName()

    /**
     * @return The description of the command
     */
    CommandDescription getDescription()

    /**
     * run the command
     *
     * @param executionContext The {@link ExecutionContext}
     *
     * @return Whether the command should continue
     */
    boolean handle(ExecutionContext executionContext)
}