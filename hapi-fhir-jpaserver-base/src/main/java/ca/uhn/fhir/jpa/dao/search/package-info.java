/**
 * Extended fhir indexing for Hibernate Search using Lucene/Elasticsearch.
 *
 * By default, Lucene indexing only provides support for _text, and _content search parameters using
 * {@link ca.uhn.fhir.jpa.model.entity.ResourceTable#myNarrativeText} and
 * {@link ca.uhn.fhir.jpa.model.entity.ResourceTable#myContentText}.
 *
 * Both {@link ca.uhn.fhir.jpa.search.builder.SearchBuilder} and {@link ca.uhn.fhir.jpa.dao.LegacySearchBuilder} delegate the
 * search to {@link ca.uhn.fhir.jpa.dao.FulltextSearchSvcImpl} when active.
 * The fulltext search runs first and interprets any search parameters it understands, returning a pid list.
 * This pid list is used as a narrowing where clause against the remaining unprocessed search parameters.
 *
 * This package extends this search to support token, string, and reference parameters via {@link ca.uhn.fhir.jpa.model.entity.ResourceTable#myLuceneIndexData}.
 * When active, the extracted search parameters which are written to the HFJ_SPIDX_* tables are also written to the Lucene index document.
 *
 * @see ca.uhn.fhir.jpa.model.search.HibernateSearchIndexWriter
 * @see ca.uhn.fhir.jpa.model.search.ExtendedLuceneIndexData
 *
 * Activated by {@link ca.uhn.fhir.jpa.api.config.DaoConfig#setAdvancedLuceneIndexing(boolean)}.
 */
package ca.uhn.fhir.jpa.dao.search;

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
