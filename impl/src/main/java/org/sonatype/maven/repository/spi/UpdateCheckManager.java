package org.sonatype.maven.repository.spi;

/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0, 
 * and you may not use this file except in compliance with the Apache License Version 2.0. 
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the Apache License Version 2.0 is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

import org.sonatype.maven.repository.Artifact;
import org.sonatype.maven.repository.ArtifactTransferException;
import org.sonatype.maven.repository.Metadata;
import org.sonatype.maven.repository.MetadataTransferException;
import org.sonatype.maven.repository.RepositorySystemSession;

/**
 * @author Benjamin Bentmann
 */
public interface UpdateCheckManager
{

    String getEffectiveUpdatePolicy( RepositorySystemSession session, String policy1, String policy2 );

    void checkArtifact( RepositorySystemSession session, UpdateCheck<Artifact, ArtifactTransferException> check );

    void touchArtifact( RepositorySystemSession session, UpdateCheck<Artifact, ArtifactTransferException> check );

    void checkMetadata( RepositorySystemSession session, UpdateCheck<Metadata, MetadataTransferException> check );

    void touchMetadata( RepositorySystemSession session, UpdateCheck<Metadata, MetadataTransferException> check );

}