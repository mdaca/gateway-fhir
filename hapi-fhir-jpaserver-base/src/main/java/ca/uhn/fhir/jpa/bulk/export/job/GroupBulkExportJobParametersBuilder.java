package ca.uhn.fhir.jpa.bulk.export.job;

/*-
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

import ca.uhn.fhir.jpa.batch.config.BatchConstants;

public class GroupBulkExportJobParametersBuilder extends BulkExportJobParametersBuilder {
	public GroupBulkExportJobParametersBuilder setGroupId(String theGroupId) {
		this.addString(BatchConstants.GROUP_ID_PARAMETER, theGroupId);
		return this;
	}

	public GroupBulkExportJobParametersBuilder setMdm(boolean theMdm) {
		this.addString(BatchConstants.EXPAND_MDM_PARAMETER, String.valueOf(theMdm));
		return this;
	}
}
