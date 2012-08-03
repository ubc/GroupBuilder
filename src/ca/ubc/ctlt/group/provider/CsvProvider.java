package ca.ubc.ctlt.group.provider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import blackboard.base.InitializationException;
import blackboard.cms.filesystem.CSContext;
import blackboard.cms.filesystem.CSEntry;
import blackboard.cms.filesystem.CSFile;
import blackboard.data.user.User;
import blackboard.platform.BbServiceException;
import ca.ubc.ctlt.group.GroUser;
import ca.ubc.ctlt.group.Group;
import ca.ubc.ctlt.group.GroupSet;
import ca.ubc.ctlt.group.Provider;
import ca.ubc.ctlt.group.UploadMultipartRequestWrapper;
import ca.ubc.ctlt.group.blackboard.BlackboardUtil;

public class CsvProvider extends Provider {
	public final static int HEADER_GROUP = 0;
	public final static int HEADER_USERNAME = 1;
	public final static int HEADER_STUDENTID = 2;
	public final static int HEADER_GROUPSET = 3;

	// possible header names to match to the headers, headers are indexed by
	// above order in this array. names do not have any specific order. All the
	// names are lower case and without space.
	private final static String[][] HEADERS = {
			{ "group", "groupname", "groupid" },
			{ "username", "userid", "id" }, { "studentid", "studentnumber" },
			{ "groupset", "set", "setname", "setid" } };

	@Override
	public Map<String, GroupSet> getGroupSets(BlackboardUtil util) {
		if (null == request) {
			log("Request object is empty!");
			return null;
		}

		Reader reader = processFile();

		if (parseCSV(util, reader)) {
			return sets;
		}

		return null;
	}

	/**
	 * There's two sources for a CSV import: A local file (one uploaded from the
	 * user's computer) and the content collections system. Once we have the csv
	 * in hand, we can pass it off to the parser, which expects the csv to be
	 * bundled in a Reader.
	 * 
	 * The local file can be passed off to the CSV parser just by grabbing the
	 * location of the uploaded file on the server.
	 * 
	 * The content system file needs to be first read into an output buffer,
	 * then converted into a string, then finally into a reader.
	 * 
	 * @return Reader for reading the file
	 */
	private Reader processFile() {
		Reader reader = null;
		UploadMultipartRequestWrapper mreq;
		if (request instanceof UploadMultipartRequestWrapper) {
			mreq = (UploadMultipartRequestWrapper) request;
		} else {
			log("Wrong class type of request!");
			return null;
		}
		log("We successfully got a multipart request.");

		String uploadtype = mreq.getParameter("csvfile_attachmentType");
		log("MulitpartRequest parsing successful!");
		if ("L".equals(uploadtype)) { // a local file is a file uploaded from
										// the user's computer
			log("Processing a local file.");
			File file = mreq.getFileFromParameterName("csvfile_LocalFile0");
			try {
				reader = new FileReader(file);
			} catch (FileNotFoundException e) {
				log("Uploaded file could not be found!");
				return null;
			}
		} else { // this means we have to get a file from the content system
			log("Processing a content collections file.");
			String path = mreq.getParameter("csvfile_CSFilePath");
			CSContext csCtx = null;
			csCtx = CSContext.getContext();
			CSEntry entry = csCtx.findEntry(path);
			CSFile file = (CSFile) entry;
			// to get the file content, I need to pass in an output stream for
			// the CSFile to write into, then I need to convert that output
			// stream
			// into a Reader
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			file.getFileContent(stream);

			reader = new StringReader(stream.toString());
		}
		log("We're done!");

		return reader;
	}

	/**
	 * Parse the CSV import.
	 * 
	 * @param util
	 *            TODO
	 * @param csvfile
	 * 
	 * @return
	 * @throws InitializationException
	 * @throws BbServiceException
	 */
	private boolean parseCSV(BlackboardUtil util, Reader csvfile) {
		CSVReader reader;
		sets = new HashMap<String, GroupSet>();
		try {
			reader = new CSVReader(csvfile);
			String[] nextLine;
			int lineNum = 2;

			// parse header and check the required columns
			Map<Integer, Integer> headerIndexes = parseHeader(reader.readNext());
			if (headerIndexes.get(HEADER_GROUP) == null
					|| (headerIndexes.get(HEADER_USERNAME) == null && headerIndexes
							.get(HEADER_STUDENTID) == null)) {
				throw new IOException(
						"Unable to parse header. Missing required column.");
			}

			while ((nextLine = reader.readNext()) != null) {
				// check the required fields
				if ((headerIndexes.get(HEADER_GROUP) == null || nextLine[headerIndexes.get(HEADER_GROUP)].trim().isEmpty()) ||
						((headerIndexes.get(HEADER_USERNAME) == null || nextLine[headerIndexes.get(HEADER_USERNAME)].trim().isEmpty()) && 
						(headerIndexes.get(HEADER_STUDENTID) == null || nextLine[headerIndexes.get(HEADER_STUDENTID)].trim().isEmpty()))) {
					throw new IOException("Required field missing on line " + lineNum + "!");
				}

				if (headerIndexes.get(HEADER_GROUPSET) == null
						|| nextLine[headerIndexes.get(HEADER_GROUPSET)].trim()
								.isEmpty()) {
					// use GroupSet.EMPTY_NAME for group set name for the file
					// without group set
					nextLine[headerIndexes.get(HEADER_GROUPSET)] = GroupSet.EMPTY_NAME;
				}

				GroupSet set = sets.get(nextLine[headerIndexes.get(HEADER_GROUPSET)].trim());
				if (set == null) {
					set = new GroupSet(nextLine[headerIndexes.get(HEADER_GROUPSET)].trim());
					sets.put(nextLine[headerIndexes.get(HEADER_GROUPSET)], set);
				}

				Group group = set.getGroup(nextLine[headerIndexes.get(HEADER_GROUP)].trim());
				if (group == null) {
					group = new Group(nextLine[headerIndexes.get(HEADER_GROUP)].trim());
					set.addGroup(group);
				}

				GroUser user = null;
				User bbUser = null;
				
				if (headerIndexes.get(HEADER_USERNAME) != null && !nextLine[headerIndexes.get(HEADER_USERNAME)].trim().isEmpty()) {
					bbUser = util.findUserByUsername(nextLine[headerIndexes.get(HEADER_USERNAME)].trim());
				} else {
					bbUser = util.findUserByStudentId(nextLine[headerIndexes.get(HEADER_STUDENTID)].trim());
				}

				if (bbUser == null) {
					throw new IOException("Could not find user in database!");
				} else {
					user = new GroUser(bbUser);
				}
				
				if (!group.hasMember(user)) {
					group.addMember(user);
				}

				lineNum++;
			}
		} catch (IOException e) {
			error("Unable to parse CSV file: " + e.getMessage());
			return false;
		}
		log("CSV parsing successful!");
		return true;
	}

	private Map<Integer, Integer> parseHeader(String[] header) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>(4);

		for (int i = 0; i < HEADERS.length; i++) {
			for (int j = 0; j < header.length; j++) {
				if (CsvProvider.isSimilarString(header[j], HEADERS[i], 1)) {
					map.put(i, j);
				}
			}
		}
		
		return map;
	}

	@Override
	public String getOptionsPage() {
		return "providers/csv/options.jsp";
	}

	@Override
	public String getName() {
		return "CSV";
	}

	@Override
	public String getDescription() {
		return "Reads in group information from a CSV file.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.ubc.ctlt.group.Provider#hasFileUpload()
	 */
	@Override
	public boolean hasFileUpload() {
		return true;
	}

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	public static int computeLevenshteinDistance(CharSequence str1,
			CharSequence str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 0; j <= str2.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = minimum(
						distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]
								+ ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
										: 1));

		return distance[str1.length()][str2.length()];
	}

	public static boolean isSimilarString(String target, String[] candidates,
			int tolerance) {
		boolean ret = false;
		target = target.trim().toLowerCase().replaceAll("\\s","");
		for (String str : candidates) {
			if (computeLevenshteinDistance(target, str) <= tolerance) {
				ret = true;
				break;
			}
		}
		return ret;
	}
}
