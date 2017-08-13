package ca.uhn.fhir.cli;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IIdType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.model.dstu2.resource.*;
import ca.uhn.fhir.model.dstu2.resource.Bundle.Entry;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;

public class ValidationDataUploader extends BaseCommand {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ValidationDataUploader.class);

	@Override
	public String getCommandDescription() {
		return "Uploads the conformance resources (StructureDefinition and ValueSet) from the official FHIR definitions.";
	}

	@Override
	public String getCommandName() {
		return "upload-definitions";
	}

	@Override
	public Options getOptions() {
		Options options = new Options();
		Option opt;

		addFhirVersionOption(options);
		
		opt = new Option("t", "target", true, "Base URL for the target server (e.g. \"http://example.com/fhir\")");
		opt.setRequired(true);
		options.addOption(opt);

		return options;
	}

	@Override
	public void run(CommandLine theCommandLine) throws ParseException {
		String targetServer = theCommandLine.getOptionValue("t");
		if (isBlank(targetServer)) {
			throw new ParseException("No target server (-t) specified");
		} else if (targetServer.startsWith("http") == false) {
			throw new ParseException("Invalid target server specified, must begin with 'http'");
		}

		FhirContext ctx = getSpecVersionContext(theCommandLine);
		if (ctx.getVersion().getVersion() == FhirVersionEnum.DSTU2) {
			uploadDefinitionsDstu2(targetServer, ctx);
		} else if (ctx.getVersion().getVersion() == FhirVersionEnum.DSTU3){
			uploadDefinitionsDstu3(targetServer, ctx);
		} else if (ctx.getVersion().getVersion() == FhirVersionEnum.R4){
			uploadDefinitionsR4(targetServer, ctx);
		}
	}

	private void uploadDefinitionsDstu2(String targetServer, FhirContext ctx) throws CommandFailureException {
		IGenericClient client = newClient(ctx, targetServer);
		ourLog.info("Uploading definitions to server: " + targetServer);

		long start = System.currentTimeMillis();

		String vsContents;
		try {
			ctx.getVersion().getPathToSchemaDefinitions();
			vsContents = IOUtils.toString(ValidationDataUploader.class.getResourceAsStream("/org/hl7/fhir/instance/model/valueset/"+"valuesets.xml"), "UTF-8");
		} catch (IOException e) {
			throw new CommandFailureException(e.toString());
		}
		Bundle bundle = ctx.newXmlParser().parseResource(Bundle.class, vsContents);

		int total = bundle.getEntry().size();
		int count = 1;
		for (Entry i : bundle.getEntry()) {
			ValueSet next = (ValueSet) i.getResource();
			next.setId(next.getIdElement().toUnqualifiedVersionless());

			ourLog.info("Uploading ValueSet {}/{} : {}", new Object[] { count, total, next.getIdElement().getValue() });
			client.update().resource(next).execute();

			count++;
		}

		try {
			vsContents = IOUtils.toString(ValidationDataUploader.class.getResourceAsStream("/org/hl7/fhir/instance/model/valueset/"+"v3-codesystems.xml"), "UTF-8");
		} catch (IOException e) {
			throw new CommandFailureException(e.toString());
		}

		bundle = ctx.newXmlParser().parseResource(Bundle.class, vsContents);
		total = bundle.getEntry().size();
		count = 1;
		for (Entry i : bundle.getEntry()) {
			ValueSet next = (ValueSet) i.getResource();
			next.setId(next.getIdElement().toUnqualifiedVersionless());

			ourLog.info("Uploading v3-codesystems ValueSet {}/{} : {}", new Object[] { count, total, next.getIdElement().getValue() });
			client.update().resource(next).execute();

			count++;
		}

		try {
			vsContents = IOUtils.toString(ValidationDataUploader.class.getResourceAsStream("/org/hl7/fhir/instance/model/valueset/"+"v2-tables.xml"), "UTF-8");
		} catch (IOException e) {
			throw new CommandFailureException(e.toString());
		}
		bundle = ctx.newXmlParser().parseResource(Bundle.class, vsContents);
		total = bundle.getEntry().size();
		count = 1;
		for (Entry i : bundle.getEntry()) {
			ValueSet next = (ValueSet) i.getResource();
			next.setId(next.getIdElement().toUnqualifiedVersionless());

			ourLog.info("Uploading v2-tables ValueSet {}/{} : {}", new Object[] { count, total, next.getIdElement().getValue() });
			client.update().resource(next).execute();
			count++;
		}

		ourLog.info("Finished uploading ValueSets");

		ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
		Resource[] mappingLocations;
		try {
			mappingLocations = patternResolver.getResources("classpath*:org/hl7/fhir/instance/model/profile/"+"*.profile.xml");
		} catch (IOException e) {
			throw new CommandFailureException(e.toString());
		}
		total = mappingLocations.length;
		count = 1;
		for (Resource i : mappingLocations) {
			StructureDefinition next;
			try {
				next = ctx.newXmlParser().parseResource(StructureDefinition.class, IOUtils.toString(i.getInputStream(), "UTF-8"));
			} catch (Exception e) {
				throw new CommandFailureException(e.toString());
			}
			next.setId(next.getIdElement().toUnqualifiedVersionless());

			ourLog.info("Uploading StructureDefinition {}/{} : {}", new Object[] { count, total, next.getIdElement().getValue() });
			try {
				client.update().resource(next).execute();
			} catch (Exception e) {
				ourLog.warn("Failed to upload {} - {}", next.getIdElement().getValue(), e.getMessage());
			}
			count++;
		}

		ourLog.info("Finished uploading ValueSets");

		long delay = System.currentTimeMillis() - start;

		ourLog.info("Finished uploading definitions to server (took {} ms)", delay);
	}

	private void uploadDefinitionsDstu3(String targetServer, FhirContext ctx) throws CommandFailureException {
		IGenericClient client = newClient(ctx, targetServer);
		ourLog.info("Uploading definitions to server: " + targetServer);

		long start = System.currentTimeMillis();
		int total = 0;
		int count = 0;
		org.hl7.fhir.dstu3.model.Bundle bundle;
		String vsContents;
		
		try {
			ctx.getVersion().getPathToSchemaDefinitions();
			vsContents = IOUtils.toString(ValidationDataUploader.class.getResourceAsStream("/org/hl7/fhir/dstu3/model/valueset/"+"valuesets.xml"), "UTF-8");
		} catch (IOException e) {
			throw new CommandFailureException(e.toString());
		}
		bundle = ctx.newXmlParser().parseResource(org.hl7.fhir.dstu3.model.Bundle.class, vsContents);

		total = bundle.getEntry().size();
		count = 1;
		for (BundleEntryComponent i : bundle.getEntry()) {
			org.hl7.fhir.dstu3.model.Resource next = i.getResource();
			next.setId(next.getIdElement().toUnqualifiedVersionless());

			int bytes = ctx.newXmlParser().encodeResourceToString(next).length();
			
			ourLog.info("Uploading ValueSet {}/{} : {} ({} bytes}", new Object[] { count, total, next.getIdElement().getValue(), bytes });
			try {
				IIdType id = client.update().resource(next).execute().getId();
				ourLog.info("  - Got ID: {}", id.getValue());
			} catch (UnprocessableEntityException e) {
				ourLog.warn("UnprocessableEntityException: " + e.toString());
			}
			count++;
		}

		try {
			vsContents = IOUtils.toString(ValidationDataUploader.class.getResourceAsStream("/org/hl7/fhir/dstu3/model/valueset/"+"v3-codesystems.xml"), "UTF-8");
		} catch (IOException e) {
			throw new CommandFailureException(e.toString());
		}

		bundle = ctx.newXmlParser().parseResource(org.hl7.fhir.dstu3.model.Bundle.class, vsContents);
		total = bundle.getEntry().size();
		count = 1;
		for (BundleEntryComponent i : bundle.getEntry()) {
			org.hl7.fhir.dstu3.model.Resource next = i.getResource();
			next.setId(next.getIdElement().toUnqualifiedVersionless());

			ourLog.info("Uploading v3-codesystems ValueSet {}/{} : {}", new Object[] { count, total, next.getIdElement().getValue() });
			client.update().resource(next).execute();

			count++;
		}

		try {
			vsContents = IOUtils.toString(ValidationDataUploader.class.getResourceAsStream("/org/hl7/fhir/dstu3/model/valueset/"+"v2-tables.xml"), "UTF-8");
		} catch (IOException e) {
			throw new CommandFailureException(e.toString());
		}
		bundle = ctx.newXmlParser().parseResource(org.hl7.fhir.dstu3.model.Bundle.class, vsContents);
		total = bundle.getEntry().size();
		count = 1;
		for (BundleEntryComponent i : bundle.getEntry()) {
			org.hl7.fhir.dstu3.model.Resource next = i.getResource();
			if (next.getIdElement().isIdPartValidLong()) {
				next.setIdElement(new IdType("v2-"+ next.getIdElement().getIdPart()));
			}
			next.setId(next.getIdElement().toUnqualifiedVersionless());

			ourLog.info("Uploading v2-tables ValueSet {}/{} : {}", new Object[] { count, total, next.getIdElement().getValue() });
			client.update().resource(next).execute();
			count++;
		}

		ourLog.info("Finished uploading ValueSets");

		
		uploadDstu3Profiles(ctx, client, "profiles-resources");
		uploadDstu3Profiles(ctx, client, "profiles-types");
		uploadDstu3Profiles(ctx, client, "profiles-others");

		ourLog.info("Finished uploading ValueSets");

		long delay = System.currentTimeMillis() - start;

		ourLog.info("Finished uploading definitions to server (took {} ms)", delay);
	}

	private void uploadDefinitionsR4(String theTargetServer, FhirContext theCtx) throws CommandFailureException {
		IGenericClient client = newClient(theCtx, theTargetServer);
		ourLog.info("Uploading definitions to server: " + theTargetServer);

		long start = System.currentTimeMillis();
		int total = 0;
		int count = 0;
		org.hl7.fhir.r4.model.Bundle bundle;
		String vsContents;

		try {
			theCtx.getVersion().getPathToSchemaDefinitions();
			vsContents = IOUtils.toString(ValidationDataUploader.class.getResourceAsStream("/org/hl7/fhir/r4/model/valueset/"+"valuesets.xml"), "UTF-8");
		} catch (IOException e) {
			throw new CommandFailureException(e.toString());
		}
		bundle = theCtx.newXmlParser().parseResource(org.hl7.fhir.r4.model.Bundle.class, vsContents);

		total = bundle.getEntry().size();
		count = 1;
		for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent i : bundle.getEntry()) {
			org.hl7.fhir.r4.model.Resource next = i.getResource();
			next.setId(next.getIdElement().toUnqualifiedVersionless());

			int bytes = theCtx.newXmlParser().encodeResourceToString(next).length();

			ourLog.info("Uploading ValueSet {}/{} : {} ({} bytes}", new Object[] { count, total, next.getIdElement().getValue(), bytes });
			try {
				IIdType id = client.update().resource(next).execute().getId();
				ourLog.info("  - Got ID: {}", id.getValue());
			} catch (UnprocessableEntityException e) {
				ourLog.warn("UnprocessableEntityException: " + e.toString());
			}
			count++;
		}

		try {
			vsContents = IOUtils.toString(ValidationDataUploader.class.getResourceAsStream("/org/hl7/fhir/r4/model/valueset/"+"v3-codesystems.xml"), "UTF-8");
		} catch (IOException e) {
			throw new CommandFailureException(e.toString());
		}

		bundle = theCtx.newXmlParser().parseResource(org.hl7.fhir.r4.model.Bundle.class, vsContents);
		total = bundle.getEntry().size();
		count = 1;
		for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent i : bundle.getEntry()) {
			org.hl7.fhir.r4.model.Resource next = i.getResource();
			next.setId(next.getIdElement().toUnqualifiedVersionless());

			ourLog.info("Uploading v3-codesystems ValueSet {}/{} : {}", new Object[] { count, total, next.getIdElement().getValue() });
			client.update().resource(next).execute();

			count++;
		}

		try {
			vsContents = IOUtils.toString(ValidationDataUploader.class.getResourceAsStream("/org/hl7/fhir/r4/model/valueset/"+"v2-tables.xml"), "UTF-8");
		} catch (IOException e) {
			throw new CommandFailureException(e.toString());
		}
		bundle = theCtx.newXmlParser().parseResource(org.hl7.fhir.r4.model.Bundle.class, vsContents);
		total = bundle.getEntry().size();
		count = 1;
		for (org.hl7.fhir.r4.model.Bundle.BundleEntryComponent i : bundle.getEntry()) {
			org.hl7.fhir.r4.model.Resource next = i.getResource();
			if (next.getIdElement().isIdPartValidLong()) {
				next.setIdElement(new org.hl7.fhir.r4.model.IdType("v2-"+ next.getIdElement().getIdPart()));
			}
			next.setId(next.getIdElement().toUnqualifiedVersionless());

			ourLog.info("Uploading v2-tables ValueSet {}/{} : {}", new Object[] { count, total, next.getIdElement().getValue() });
			client.update().resource(next).execute();
			count++;
		}

		ourLog.info("Finished uploading ValueSets");


		uploadDstu3Profiles(theCtx, client, "profiles-resources");
		uploadDstu3Profiles(theCtx, client, "profiles-types");
		uploadDstu3Profiles(theCtx, client, "profiles-others");

		ourLog.info("Finished uploading ValueSets");

		long delay = System.currentTimeMillis() - start;

		ourLog.info("Finished uploading definitions to server (took {} ms)", delay);
	}

	private void uploadDstu3Profiles(FhirContext ctx, IGenericClient client, String name) throws CommandFailureException {
		int total;
		int count;
		org.hl7.fhir.dstu3.model.Bundle bundle;
		ourLog.info("Uploading " + name);
		String vsContents;
		try {
			vsContents = IOUtils.toString(ValidationDataUploader.class.getResourceAsStream("/org/hl7/fhir/dstu3/model/profile/" + name + ".xml"), "UTF-8");
		} catch (IOException e) {
			throw new CommandFailureException(e.toString());
		}

		bundle = ctx.newXmlParser().parseResource(org.hl7.fhir.dstu3.model.Bundle.class, vsContents);
		total = bundle.getEntry().size();
		count = 1;
		
		Collections.sort(bundle.getEntry(), new Comparator<BundleEntryComponent>() {
			@Override
			public int compare(BundleEntryComponent theO1, BundleEntryComponent theO2) {
				if (theO1.getResource() == null && theO2.getResource() == null) {
					return 0;
				}
				if (theO1.getResource() == null) {
					return 1;
				}
				if (theO2.getResource() == null) {
					return -1;
				}
				// StructureDefinition, then OperationDefinition, then CompartmentDefinition
				return theO2.getResource().getClass().getName().compareTo(theO1.getResource().getClass().getName());
			}});
		
		for (BundleEntryComponent i : bundle.getEntry()) {
			org.hl7.fhir.dstu3.model.Resource next = i.getResource();
			next.setId(next.getIdElement().toUnqualifiedVersionless());
			if (next instanceof CapabilityStatement) {
				continue;
			}
	
			ourLog.info("Uploading {} StructureDefinition {}/{} : {}", new Object[] { name, count, total, next.getIdElement().getValue() });
			client.update().resource(next).execute();
	
			count++;
		}
	}

}
