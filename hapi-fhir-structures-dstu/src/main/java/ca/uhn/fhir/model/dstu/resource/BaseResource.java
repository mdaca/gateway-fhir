package ca.uhn.fhir.model.dstu.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/*
 * #%L
 * HAPI FHIR Structures - DSTU1 (FHIR v0.80)
 * %%
 * Copyright (C) 2014 - 2017 University Health Network
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

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hl7.fhir.instance.model.api.IBaseCoding;
import org.hl7.fhir.instance.model.api.IBaseMetaType;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.instance.model.api.IPrimitiveType;

import ca.uhn.fhir.model.api.BaseElement;
import ca.uhn.fhir.model.api.ICompositeElement;
import ca.uhn.fhir.model.api.IElement;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.api.Tag;
import ca.uhn.fhir.model.api.TagList;
import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.SearchParamDefinition;
import ca.uhn.fhir.model.base.composite.BaseCodingDt;
import ca.uhn.fhir.model.base.resource.ResourceMetadataMap;
import ca.uhn.fhir.model.dstu.composite.CodingDt;
import ca.uhn.fhir.model.dstu.composite.ContainedDt;
import ca.uhn.fhir.model.dstu.composite.NarrativeDt;
import ca.uhn.fhir.model.primitive.CodeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.util.ElementUtil;

public abstract class BaseResource extends BaseElement implements IResource {

	/**
	 * <b>Fluent Client</b> search parameter constant for <b>_id</b>
	 * <p>
	 * Description: <b>the _id of a resource</b><br>
	 * Type: <b>string</b><br>
	 * Path: <b>Resource._id</b><br>
	 * </p>
	 */
	public static final StringClientParam RES_ID = new StringClientParam(BaseResource.SP_RES_ID);


	/**
	 * Search parameter constant for <b>_id</b>
	 */
	@SearchParamDefinition(name="_id", path="", description="The ID of the resource", type="string"  )
	public static final String SP_RES_ID = "_id";

	@Child(name = "contained", order = 2, min = 0, max = 1)
	private ContainedDt myContained;

	private IdDt myId;

	@Child(name = "language", order = 0, min = 0, max = 1)
	private CodeDt myLanguage;

	private ResourceMetadataMap myResourceMetadata;

	@Child(name = "text", order = 1, min = 0, max = 1)
	private NarrativeDt myText;

	/**
	 * NOP implementation of this method. 
	 * 
	 * @see ICompositeElement#getAllPopulatedChildElementsOfType(Class) for an explanation of why you
	 * don't need to override this method
	 */
	@Override
	public <T extends IElement> List<T> getAllPopulatedChildElementsOfType(Class<T> theType) {
		return Collections.emptyList();
	}

	@Override
	public ContainedDt getContained() {
		if (myContained == null) {
			myContained = new ContainedDt();
		}
		return myContained;
	}

	public IdDt getId() {
		if (myId == null) {
			myId = new IdDt();
		}
		return myId;
	}

	@Override
	public IIdType getIdElement() {
		return getId();
	}

	@Override
	public CodeDt getLanguage() {
		if (myLanguage == null) {
			myLanguage = new CodeDt();
		}
		return myLanguage;
	}

	@Override
	public IBaseMetaType getMeta() {
		return new IBaseMetaType() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public IBaseMetaType addProfile(String theProfile) {
				ArrayList<IdDt> newTagList = new ArrayList<IdDt>();
				List<IdDt> existingTagList = ResourceMetadataKeyEnum.PROFILES.get(BaseResource.this);
				if (existingTagList != null) {
					newTagList.addAll(existingTagList);
				}
				ResourceMetadataKeyEnum.PROFILES.put(BaseResource.this, newTagList);
				
				IdDt tag = new IdDt(theProfile);
				newTagList.add(tag);
				return this;
			}
			
			@Override
			public IBaseCoding addSecurity() {
				List<BaseCodingDt> tagList = ResourceMetadataKeyEnum.SECURITY_LABELS.get(BaseResource.this);
				if (tagList == null) {
					tagList = new ArrayList<BaseCodingDt>();
					ResourceMetadataKeyEnum.SECURITY_LABELS.put(BaseResource.this, tagList);
				}
				CodingDt tag = new CodingDt();
				tagList.add(tag);
				return asBaseCoding(tag);
			}
			
			@Override
			public IBaseCoding addTag() {
				TagList tagList = ResourceMetadataKeyEnum.TAG_LIST.get(BaseResource.this);
				if (tagList == null) {
					tagList = new TagList();
					ResourceMetadataKeyEnum.TAG_LIST.put(BaseResource.this, tagList);
				}
				Tag tag = new Tag();
				tagList.add(tag);
				return tag;
			}
			
			/**
			 * This view is used because the old DSTU1 BaseCodingDt can't implements IBaseCoding
			 */
			private IBaseCoding asBaseCoding(final BaseCodingDt theCoding) {
				return new IBaseCoding() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getCode() {
						return theCoding.getCodeElement().getValue();
					}
					
					@Override
					public String getDisplay() {
						return theCoding.getDisplayElement().getValue();
					}
					
					@Override
					public List<String> getFormatCommentsPost() {
						return Collections.emptyList();
					}
					
					@Override
					public List<String> getFormatCommentsPre() {
						return Collections.emptyList();
					}
					
					@Override
					public String getSystem() {
						return theCoding.getSystemElement().getValue();
					}
					
					@Override
					public boolean hasFormatComment() {
						return false;
					}
					
					@Override
					public boolean isEmpty() {
						return ElementUtil.isEmpty(getSystem(), getCode(), getDisplay());
					}
					
					@Override
					public IBaseCoding setCode(String theTerm) {
						theCoding.setCode(theTerm);
						return this;
					}
					
					@Override
					public IBaseCoding setDisplay(String theLabel) {
						theCoding.setDisplay(theLabel);
						return this;
					}
					
					@Override
					public IBaseCoding setSystem(String theScheme) {
						theCoding.setSystem(theScheme);
						return this;
					}
				};
			}
			
			@Override
			public List<String> getFormatCommentsPost() {
				return Collections.emptyList();
			}
			
			@Override
			public List<String> getFormatCommentsPre() {
				return Collections.emptyList();
			}
			
			@Override
			public Date getLastUpdated() {
				InstantDt lu = ResourceMetadataKeyEnum.UPDATED.get(BaseResource.this);
				if (lu != null) {
					return lu.getValue();
				}
				return null;
			}
			
			@Override
			public List<? extends IPrimitiveType<String>> getProfile() {
				ArrayList<IPrimitiveType<String>> retVal = new ArrayList<IPrimitiveType<String>>();
				List<IdDt> profilesList = ResourceMetadataKeyEnum.PROFILES.get(BaseResource.this);
				if (profilesList == null) {
					return Collections.emptyList();
				}
				for (IdDt next : profilesList) {
					retVal.add(next);
				}
				return Collections.unmodifiableList(retVal);
			}

			@Override
			public List<? extends IBaseCoding> getSecurity() {
				ArrayList<IBaseCoding> retVal = new ArrayList<IBaseCoding>();
				List<BaseCodingDt> labelsList = ResourceMetadataKeyEnum.SECURITY_LABELS.get(BaseResource.this);
				if (labelsList == null) {
					return Collections.emptyList();
				}
				for (BaseCodingDt next : labelsList) {
					retVal.add(asBaseCoding(next));
				}
				return Collections.unmodifiableList(retVal);
			}
			
			@Override
			public IBaseCoding getSecurity(String theSystem, String theCode) {
				for (BaseCodingDt next : ResourceMetadataKeyEnum.SECURITY_LABELS.get(BaseResource.this)) {
					if (theSystem.equals(next.getSystemElement().getValue()) && theCode.equals(next.getCodeElement().getValue())) {
						return asBaseCoding(next);
					}
				}
				return null;
			}
			
			@Override
			public List<? extends IBaseCoding> getTag() {
				ArrayList<IBaseCoding> retVal = new ArrayList<IBaseCoding>();
				for (Tag next : ResourceMetadataKeyEnum.TAG_LIST.get(BaseResource.this)) {
					retVal.add(next);
				}
				return Collections.unmodifiableList(retVal);
			}
			
			@Override
			public IBaseCoding getTag(String theSystem, final String theCode) {
				for (final Tag next : ResourceMetadataKeyEnum.TAG_LIST.get(BaseResource.this)) {
					if (next.getScheme().equals(theSystem) && next.getTerm().equals(theCode)) {
						return next;
					}
				}
				return null;
			}
			
			@Override
			public String getVersionId() {
				return getId().getVersionIdPart();
			}
			
			@Override
			public boolean hasFormatComment() {
				return false;
			}
			
			@Override
			public boolean isEmpty() {
				return getResourceMetadata().isEmpty();
			}
			
			@Override
			public IBaseMetaType setLastUpdated(Date theHeaderDateValue) {
				ResourceMetadataKeyEnum.UPDATED.put(BaseResource.this, new InstantDt(theHeaderDateValue));
				return this;
			}
			

			@Override
			public IBaseMetaType setVersionId(String theVersionId) {
				setId(getId().withVersion(theVersionId));
				return this;
			}
		};
	}

	@Override
	public ResourceMetadataMap getResourceMetadata() {
		if (myResourceMetadata == null) {
			myResourceMetadata = new ResourceMetadataMap();
		}
		return myResourceMetadata;
	}

	@Override
	public NarrativeDt getText() {
		if (myText == null) {
			myText = new NarrativeDt();
		}
		return myText;
	}

	/**
	 * Intended to be called by extending classes {@link #isEmpty()} implementations, returns <code>true</code> if all
	 * content in this superclass instance is empty per the semantics of {@link #isEmpty()}.
	 */
	@Override
	protected boolean isBaseEmpty() {
		return super.isBaseEmpty() && ElementUtil.isEmpty(myLanguage, myText, myId);
	}

	public void setContained(ContainedDt theContained) {
		myContained = theContained;
	}

	public void setId(IdDt theId) {
		myId = theId;
	}

	public BaseResource setId(IIdType theId) {
		if (theId instanceof IdDt) {
			myId = (IdDt) theId;
		} else if (theId != null) {
			myId = new IdDt(theId.getValue());
		}
		return this;
	}

	public BaseResource setId(String theId) {
		if (theId == null) {
			myId = null;
		} else {
			myId = new IdDt(theId);
		}
		return this;
	}

	@Override
	public void setLanguage(CodeDt theLanguage) {
		myLanguage = theLanguage;
	}

	@Override
	public void setResourceMetadata(ResourceMetadataMap theMap) {
		Validate.notNull(theMap, "The Map must not be null");
		myResourceMetadata = theMap;
	}

	public void setText(NarrativeDt theText) {
		myText = theText;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
		b.append("id", getId().toUnqualified());
		return b.toString();
	}

}
