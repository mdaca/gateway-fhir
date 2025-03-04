package ca.uhn.fhir.jpa.bulk.imprt.api;

/*-
 * #%L
 * HAPI FHIR Storage api
 * %%
 * Copyright (C) 2014 - 2021 Smile CDR, Inc.
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

import ca.uhn.fhir.jpa.bulk.imprt.model.BulkImportJobFileJson;
import ca.uhn.fhir.jpa.bulk.imprt.model.BulkImportJobJson;
import ca.uhn.fhir.jpa.bulk.imprt.model.BulkImportJobStatusEnum;

import javax.annotation.Nonnull;
import java.util.List;

public interface IBulkDataImportSvc {

	/**
	 * Create a new job in {@link ca.uhn.fhir.jpa.bulk.imprt.model.BulkImportJobStatusEnum#STAGING STAGING} state (meaning it won't yet be worked on and can be added to)
	 */
	String createNewJob(BulkImportJobJson theJobDescription, @Nonnull List<BulkImportJobFileJson> theInitialFiles);

	/**
	 * Add more files to a job in {@link ca.uhn.fhir.jpa.bulk.imprt.model.BulkImportJobStatusEnum#STAGING STAGING} state
	 *
	 * @param theJobId The job ID
	 * @param theFiles The files to add to the job
	 */
	void addFilesToJob(String theJobId, List<BulkImportJobFileJson> theFiles);

	/**
	 * Move a job from {@link ca.uhn.fhir.jpa.bulk.imprt.model.BulkImportJobStatusEnum#STAGING STAGING}
	 * state to {@link ca.uhn.fhir.jpa.bulk.imprt.model.BulkImportJobStatusEnum#READY READY}
	 * state, meaning that is is a candidate to be picked up for processing
	 *
	 * @param theJobId The job ID
	 */
	void markJobAsReadyForActivation(String theJobId);

	/**
	 * This method is intended to be called from the job scheduler, and will begin execution on
	 * the next job in status {@link ca.uhn.fhir.jpa.bulk.imprt.model.BulkImportJobStatusEnum#READY READY}
	 *
	 * @return Returns {@literal true} if a job was activated
	 */
	boolean activateNextReadyJob();

	/**
	 * Updates the job status for the given job
	 */
	void setJobToStatus(String theJobId, BulkImportJobStatusEnum theStatus);

	/**
	 * Updates the job status for the given job
	 */
	void setJobToStatus(String theJobId, BulkImportJobStatusEnum theStatus, String theStatusMessage);

	/**
	 * Gets the number of files available for a given Job ID
	 *
	 * @param theJobId The job ID
	 * @return The file count
	 */
	BulkImportJobJson fetchJob(String theJobId);

	/**
	 * Fetch a given file by job ID
	 *
	 * @param theJobId     The job ID
	 * @param theFileIndex The index of the file within the job
	 * @return The file
	 */
	BulkImportJobFileJson fetchFile(String theJobId, int theFileIndex);

	/**
	 * Delete all input files associated with a particular job
	 */
	void deleteJobFiles(String theJobId);

	/**
	 * Fetch just the file description for the given file
	 */
	String getFileDescription(String theJobId, int theFileIndex);
}
