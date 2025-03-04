package ca.uhn.fhir.jpa.term.job;

/*
 * #%L
 * HAPI FHIR JPA Server
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

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;

import static ca.uhn.fhir.jpa.batch.config.BatchConstants.JOB_PARAM_CODE_SYSTEM_VERSION_ID;

/**
 * Validates that a TermCodeSystem parameter is present
 */
public class TermCodeSystemVersionDeleteJobParameterValidator implements JobParametersValidator {

	@Override
	public void validate(JobParameters theJobParameters) throws JobParametersInvalidException {
		if (theJobParameters == null) {
			throw new JobParametersInvalidException("This job needs Parameter: '" + JOB_PARAM_CODE_SYSTEM_VERSION_ID + "'");
		}

		if ( ! theJobParameters.getParameters().containsKey(JOB_PARAM_CODE_SYSTEM_VERSION_ID)) {
			throw new JobParametersInvalidException("This job needs Parameter: '" + JOB_PARAM_CODE_SYSTEM_VERSION_ID + "'");
		}

		Long termCodeSystemPid = theJobParameters.getLong(JOB_PARAM_CODE_SYSTEM_VERSION_ID);
		if (termCodeSystemPid == null) {
			throw new JobParametersInvalidException("'" + JOB_PARAM_CODE_SYSTEM_VERSION_ID + "' parameter is null");
		}

		if (termCodeSystemPid <= 0) {
			throw new JobParametersInvalidException(
				"Invalid parameter '" + JOB_PARAM_CODE_SYSTEM_VERSION_ID + "' value: " + termCodeSystemPid);
		}
	}
}
