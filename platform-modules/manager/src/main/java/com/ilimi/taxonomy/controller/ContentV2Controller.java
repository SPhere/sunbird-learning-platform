package com.ilimi.taxonomy.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ilimi.common.controller.BaseController;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.logger.LogHelper;
import com.ilimi.taxonomy.enums.ContentAPIParams;
import com.ilimi.taxonomy.enums.ContentErrorCodes;
import com.ilimi.taxonomy.mgr.IContentManager;

/**
 * The Class ContentV2Controller, is the main entry point for the High Level
 * Content Operations, mostly it holds the API Method related to Content
 * Workflow Management such as 'Upload', 'Publish' 'Optimize', 'Extract' and
 * 'Bundle'. Other that these operation the Content can have other basic CRUD
 * Operations.
 * 
 * All the Methods are backed by their corresponding managers, which have the
 * actual logic to communicate with the middleware and core level APIs.
 * 
 * @author Azhar
 */
@Controller
@RequestMapping("/v2/content")
public class ContentV2Controller extends BaseController {

	/** The Class Logger. */
	private static LogHelper LOGGER = LogHelper.getInstance(ContentV2Controller.class.getName());

	@Autowired
	private ContentController contentController;

	@Autowired
	private IContentManager contentManager;

	/** The graph id. */
	private String graphId = "domain";

	/**
	 * This method carries all the tasks related to 'Upload' operation of
	 * content work-flow.
	 * 
	 *
	 * @param contentId
	 *            The Content Id for which the Content Package needs to be
	 *            Uploaded.
	 * @param file
	 *            The Content Package File
	 * @param userId
	 *            Unique id of the user mainly for authentication purpose, It
	 *            can impersonation details as well.
	 * @return The Response entity with Content Id in its Result Set.
	 */
	@RequestMapping(value = "/upload/{id:.+}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> upload(@PathVariable(value = "id") String contentId,
			@RequestParam(value = "file", required = true) MultipartFile file,
			@RequestHeader(value = "user-id") String userId) {

		LOGGER.debug("Upload Content | Content Id: " + contentId);

		LOGGER.info("Uploaded File Name: " + file.getName());
		LOGGER.info("User Id: " + userId);

		LOGGER.info("Calling the Manager for 'Upload' Operation | [Content Id " + contentId + "]");
		return contentController.upload(contentId, file, "domain", userId, null);
	}

	/**
	 * This method carries all the tasks related to 'Publish' operation of
	 * content work-flow.
	 *
	 * @param contentId
	 *            The Content Id which needs to be published.
	 * @param userId
	 *            Unique 'id' of the user mainly for authentication purpose, It
	 *            can impersonation details as well.
	 * @return The Response entity with Content Id and ECAR URL in its Result
	 *         Set.
	 */
	@RequestMapping(value = "/publish/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> publish(@PathVariable(value = "id") String contentId,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "content.publish";
		LOGGER.info("Publish content | Content Id : " + contentId);
		try {
			LOGGER.info("Calling the Manager for 'Publish' Operation | [Content Id " + contentId + "]");
			Response response = contentManager.publish(graphId, contentId);

			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * This method carries all the tasks related to 'Optimize' operation of
	 * content work-flow. This includes compressing images, audio and videos.
	 *
	 * @param contentId
	 *            Content Id which needs to be optimized.
	 * @param userId
	 *            Unique 'id' of the user mainly for authentication purpose, It
	 *            can impersonation details as well.
	 * @return The Response entity with Content Id in its Result Set.
	 */
	@RequestMapping(value = "/optimize/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> optimize(@PathVariable(value = "id") String contentId,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "content.optimize";
		LOGGER.info("Optimize content | Content Id : " + contentId);
		try {
			LOGGER.info("Calling the Manager for 'Optimize' Operation | [Content Id " + contentId + "]");
			Response response = contentManager.optimize(graphId, contentId);

			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * This method carries all the tasks related to 'Extract' operation of
	 * content work-flow. This includes Uploading of Asset Items to the Global
	 * Storage (S3), Creation of Asset Objects in Graph, Creation of Assessment
	 * Item into the Graph and Update the body of Content (In Case of ECML Type
	 * Content).
	 *
	 * @param contentId
	 *            Content Id which needs to be extracted.
	 * @param userId
	 *            Unique 'id' of the user mainly for authentication purpose, It
	 *            can impersonation details as well.
	 * @return The Response entity with Content Id in its Result Set.
	 */
	@RequestMapping(value = "/extract/{id:.+}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> extract(@PathVariable(value = "id") String contentId,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "content.extract";
		LOGGER.info("Extract content | Content Id : " + contentId);
		try {
			LOGGER.info("Calling the Manager for 'Extract' Operation | [Content Id " + contentId + "]");
			Response response = contentManager.extract(graphId, contentId);

			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	/**
	 * This method carries all the tasks related of bundling the contents into
	 * one package, It includes all the operations valid for the Publish
	 * operation but without making the status of content as 'Live'. i.e. It
	 * bundles content of all status with a 'expiry' date.
	 *
	 * @param map
	 *            the map contains the parameter for creating the Bundle e.g.
	 *            "identifier" List.
	 * @param userId
	 *            Unique 'id' of the user mainly for authentication purpose, It
	 *            can impersonation details as well.
	 * @return The Response entity with a Bundle URL in its Result Set.
	 */
	@RequestMapping(value = "/bundle", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Response> bundle(@RequestBody Map<String, Object> map,
			@RequestHeader(value = "user-id") String userId) {
		String apiId = "content.archive";
		LOGGER.info("Create Content Bundle | user-id: " + userId);
		try {
			Request request = getBundleRequest(map, ContentErrorCodes.ERR_CONTENT_INVALID_BUNDLE_CRITERIA.name());
			request.put(ContentAPIParams.version.name(), "v2");

			LOGGER.info("Calling the Manager for 'Bundle' Operation");
			Response response = contentManager.bundle(request, graphId, "1.1");
			LOGGER.info("Archive | Response: " + response);

			return getResponseEntity(response, apiId, null);
		} catch (Exception e) {
			return getExceptionResponseEntity(e, apiId, null);
		}
	}

	protected String getAPIVersion() {
		return API_VERSION_2;
	}
}
