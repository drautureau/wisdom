/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
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
 * #L%
 */
package org.wisdom.maven.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.wisdom.maven.Constants;
import org.wisdom.maven.WatchingException;
import org.wisdom.maven.utils.ResourceCopy;
import org.wisdom.maven.utils.WatcherUtils;

import java.io.File;
import java.io.IOException;

/**
 * Copy configuration files.
 */
@Mojo(name = "copy-configuration", threadSafe = false,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class CopyConfigurationMojo extends AbstractWisdomWatcherMojo implements Constants {

    private File source;
    private File destination;

    @Component
    private MavenResourcesFiltering filtering;

    /**
     * The set of extension to add to the list of non-filtered resources.
     * Extensions are given without the ".".
     */
    @Parameter
    String[] nonFilteredExtensions;

    /**
     * Execute copies the resources from the configuration directory to the server configuration
     * if the {@link #wisdomDirectory} is not set.
     *
     * @throws MojoExecutionException when the copy fails.
     */
    @Override
    public void execute() throws MojoExecutionException {
        if (wisdomDirectory != null) {
            getLog().info("Skipping the Configuration copy as we are using a remote Wisdom " +
                    "Server");
            removeFromWatching();
            return;
        }
        source = new File(basedir, CONFIGURATION_SRC_DIR);
        destination = new File(getWisdomRootDirectory(), CONFIGURATION_DIR);

        if (nonFilteredExtensions != null) {
            ResourceCopy.addNonFilteredExtension(nonFilteredExtensions);
        }

        try {
            ResourceCopy.copyConfiguration(this, filtering);
        } catch (IOException e) {
            throw new MojoExecutionException("Error during configuration copy", e);
        }
    }

    @Override
    public boolean accept(File file) {
        return WatcherUtils.isInDirectory(file, WatcherUtils.getConfigurationSource(basedir));
    }

    @Override
    public boolean fileCreated(File file) throws WatchingException {
        try {
            ResourceCopy.copyFileToDir(file, source, destination, this, filtering, null);
        } catch (IOException e) {
            throw new WatchingException(e.getMessage(), file, e);
        }
        getLog().info(file.getName() + " copied to the configuration directory");
        return false;
    }

    @Override
    public boolean fileUpdated(File file) throws WatchingException {
        try {
            ResourceCopy.copyFileToDir(file, source, destination, this, filtering, null);
        } catch (IOException e) {
            throw new WatchingException(e.getMessage(), file, e);
        }
        getLog().info(file.getName() + " updated in the configuration directory");
        return false;
    }

    @Override
    public boolean fileDeleted(File file) throws WatchingException {
        File copied = ResourceCopy.computeRelativeFile(file, source, destination);
        if (copied.exists()) {
            copied.delete();
        }
        getLog().info(copied.getName() + " deleted");
        return false;
    }

}
