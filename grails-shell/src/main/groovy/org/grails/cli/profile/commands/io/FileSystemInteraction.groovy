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
package org.grails.cli.profile.commands.io

import grails.build.logging.GrailsConsole
import groovy.transform.CompileStatic
import org.grails.cli.profile.ExecutionContext
import org.grails.io.support.DefaultResourceLoader
import org.grails.io.support.FileSystemResource
import org.grails.io.support.PathMatchingResourcePatternResolver
import org.grails.io.support.Resource
import org.grails.io.support.ResourceLoader
import org.grails.io.support.ResourceLocator
import org.grails.io.support.SpringIOUtils


/**
 * Utility methods exposed to scripts for interacting with resources (found on the file system or jars) and the file system
 *
 * @author Graeme Rocher
 * @since 3.0
 */
@CompileStatic
class FileSystemInteraction {

    ExecutionContext executionContext
    ResourceLoader resourceLoader
    PathMatchingResourcePatternResolver resourcePatternResolver
    ResourceLocator resourceLocator

    FileSystemInteraction(ExecutionContext executionContext, ResourceLoader resourceLoader = new DefaultResourceLoader()) {
        this.executionContext = executionContext
        this.resourceLoader = resourceLoader
        this.resourceLocator = new ResourceLocator()
        this.resourceLocator.setSearchLocation(executionContext.baseDir.absolutePath)
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver(resourceLoader)
    }

    /**
     * Makes a directory
     *
     * @param path The path to the directory
     */
    FileSystemInteraction mkdir(path) {
        file(path)?.mkdirs()
        return this
    }

    /**
     * Deletes a file
     *
     * @param path The path to the file
     */
    FileSystemInteraction delete(path) {
        file(path)?.delete()
        return this
    }

    /**
     * Allows Gradle style simple copy specs
     *
     * @param callable The callable
     * @return this
     */
    FileSystemInteraction copy(@DelegatesTo(CopySpec) Closure callable) {
        CopySpec spec = new CopySpec()
        callable.delegate = spec
        callable.call()
        if(spec.from && spec.into) {
            if(spec.from instanceof Iterable) {
                copyAll((Iterable)spec.from, spec.into)
            }
            else {
                copy(spec.from, spec.into)
            }
        }
        return this
    }
    /**
     * Copies a resource to the target destination
     *
     * @param path The path
     * @param destination The destination
     */
    FileSystemInteraction copy(path, destination) {
        def from = resource(path)
        def to = file(destination)
        copy(from, to)
        return this
    }

    /**
     * Copies resources to the target destination
     *
     * @param path The path
     * @param destination The destination
     */
    FileSystemInteraction copyAll(Iterable resources, destination) {
        for(path in resources) {
            def from = resource(path)
            def to = file(destination)
            copy(from, to)
        }
        return this
    }

    /**
     * Copy a Resource from the given location to the given directory or location
     *
     * @param from The resource to copy
     * @param to The location to copy to
     * @return The {@FileSystemInteraction} instance
     */
    FileSystemInteraction copy(Resource from, File to) {
        if (from && to) {
            if (to.isDirectory()) {
                mkdir(to)
                to = new File(to, from.filename)
            }
            SpringIOUtils.copy(from, to)
            GrailsConsole.instance.addStatus("Copied ${from.filename} to location ${to.canonicalPath}")
        }
        return this
    }

    /**
     * Obtain a file for the given path
     *
     * @param path The path
     * @return The file
     */
    File file(Object path) {
        if(path instanceof File) return (File)path
        else {
            def baseDir = executionContext.baseDir
            new File(baseDir ?: new File("."), path.toString())
        }
    }

    /**
     * Finds a source file for the given class name
     * @param className The class name
     * @return The source resource
     */
    Resource source(String className) {
        resourceLocator.findResourceForClassName(className)
    }

    /**
     * Obtain a resource for the given path
     * @param path The path
     * @return The resource
     */
    Resource resource(Object path) {
        if(!path) return null
        if(path instanceof Resource) return (Resource)path
        def f = file(path)
        if(f?.exists() && f.isFile()) {
            return new FileSystemResource(f)
        }
        else {
            def pathStr = path.toString()
            def resource = resourceLoader.getResource(pathStr)
            if(resource.exists()) {
                return resource
            }
            else {
                def allResources = resources(pathStr)
                if(allResources) {
                    return allResources[0]
                }
                else {
                    return resource
                }
            }
        }
    }

    /**
     * Obtain resources for the given pattern
     *
     * @param pattern The pattern
     * @return The resources
     */
    Collection<Resource> resources(String pattern) {
        try {
            return resourcePatternResolver.getResources(pattern).toList()
        } catch (e) {
            return []
        }
    }

    /**
     * Obtain the path of the resource relative to the current project
     *
     * @param path The path to inspect
     * @return The relative path
     */
    String projectPath(Object path) {
        def file = file(path)
        if(file) {
            def basePath = executionContext.baseDir.canonicalPath
            return (file.canonicalPath - basePath).substring(1)
        }
        return ""
    }

    /**
     * Get files matching the given pattern
     *
     * @param pattern The pattern
     * @return the files
     */
    Collection<File> files(String pattern) {
        resources(pattern).collect() { Resource res -> res.file }
    }

    static class CopySpec {
        def from
        def into
        void from(path) {
            this.from = path
        }
        void into(path) {
            this.into = path
        }
    }
}
