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
package org.grails.cli.profile.commands.script

import grails.build.logging.ConsoleLogger
import grails.build.logging.GrailsConsole
import grails.util.Environment
import grails.util.GrailsNameUtils
import groovy.transform.CompileStatic
import org.grails.cli.profile.CommandDescription
import org.grails.cli.profile.ExecutionContext
import org.grails.cli.profile.Profile
import org.grails.cli.profile.ProfileCommand
import org.grails.cli.profile.codegen.ModelBuilder
import org.grails.cli.profile.commands.io.FileSystemInteraction
import org.grails.cli.profile.commands.templates.TemplateRenderer

/**
 * A base class for Groovy scripts that implement commands
 *
 * @author Graeme Rocher
 * @since 3.0
 */
@CompileStatic
abstract class CommandScript extends Script implements ProfileCommand, ConsoleLogger {

    Profile profile
    String name = getClass().name.contains('-') ? getClass().name : GrailsNameUtils.getScriptName(getClass().name)
    CommandDescription description = new CommandDescription(name)
    @Delegate ExecutionContext executionContext
    @Delegate TemplateRenderer templateRenderer
    @Delegate ModelBuilder modelBuilder = new ModelBuilder()
    @Delegate ConsoleLogger consoleLogger = GrailsConsole.getInstance()
    @Delegate FileSystemInteraction fileSystemInteraction
    AntBuilder ant = new AntBuilder()

    /**
     * The location of the user.home directory
     */
    String userHome = System.getProperty('user.home')
    /**
     * The version of Grails being used
     */
    String grailsVersion = getClass().getPackage()?.getImplementationVersion()

    /**
     * Provides a description for the command
     *
     * @param desc The description
     * @param usage The usage information
     */
    void description(String desc, String usage) {
        this.description = new CommandDescription(name, desc, usage)
    }

    /**
     * @return The undeclared command line arguments
     */
    Map<String, Object> getArgsMap() {
        executionContext.commandLine.undeclaredOptions
    }

    /**
     * @return The arguments as a list of strings
     */
    List<String> getArgs() {
        executionContext.commandLine.remainingArgs
    }

    /**
     * @return The name of the current Grails environment
     */
    String getGrailsEnv() {  Environment.current.name }

    /**
     * @return The {@link GrailsConsole} instance
     */
    GrailsConsole getGrailsConsole() { executionContext.console }

    @Override
    boolean handle(ExecutionContext executionContext) {
        setExecutionContext(executionContext)
        run()
        return true
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext
        this.consoleLogger = executionContext.console
        this.templateRenderer = new TemplateRenderer(executionContext)
        this.fileSystemInteraction = new FileSystemInteraction(executionContext)
    }

    ExecutionContext getExecutionContext() {
        return executionContext
    }
}
