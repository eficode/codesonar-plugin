package org.jenkinsci.plugins.codesonar.services;

import hudson.AbortException;
import org.apache.http.client.utils.URIBuilder;
import org.jenkinsci.plugins.codesonar.models.SearchResults;
import org.jenkinsci.plugins.codesonar.models.analysis.Analysis;
import org.jenkinsci.plugins.codesonar.models.projects.Project40;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Andrius
 */
public class AnalysisService42 implements IAnalysisService {

    private HttpService httpService;
    private XmlSerializationService xmlSerializationService;

    private enum UrlFilters {

        NEW("5"), ACTIVE("2");

        private final String value;

        private UrlFilters(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }
    
    public AnalysisService42(HttpService httpService, XmlSerializationService xmlSerializationService) {
        this.httpService = httpService;
        this.xmlSerializationService = xmlSerializationService;
    }

    @Override
    public String getAnalysisUrlFromLogFile(List<String> logFile) {
        Pattern pattern = Pattern.compile("codesonar:\\s+(.*/analysis/.*)");

        String analysisUrl = null;
        for (String line : logFile) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                analysisUrl = matcher.group(1);
            }
        }

        if (analysisUrl != null && analysisUrl.endsWith(".html")) {
            analysisUrl = analysisUrl.replaceAll(".html", ".xml");
        }

        return analysisUrl;
    }

    @Override
    public String getLatestAnalysisUrlForAProject(URI baseHubUri, String projectName) throws IOException {
        String encode = URLEncoder.encode("\"" + projectName + "\"", "UTF-8");
        URI uri = baseHubUri.resolve("/project_search.xml?query=" + encode + "&scope=all");

        String xmlContent = httpService.getContentFromUrlAsString(uri);

        SearchResults searchResults = xmlSerializationService.deserialize(xmlContent, SearchResults.class);
        Project40 project = searchResults.getProjectByName(projectName);

        return baseHubUri.resolve(project.getUrl()).toString();
    }

    @Override
    public Analysis getAnalysisFromUrl(String analysisUrl) throws IOException {
        String xmlContent = httpService.getContentFromUrlAsString(analysisUrl);

        return xmlSerializationService.deserialize(xmlContent, Analysis.class);
    }
    
    @Override
    public Analysis getAnalysisFromUrlWithNewWarnings(String analysisUrl) throws IOException {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(analysisUrl);
            uriBuilder.addParameter("filter", UrlFilters.NEW.getValue());
        } catch (URISyntaxException ex) {
            throw new AbortException(ex.getMessage());
        }
        
        return getAnalysisFromUrl(uriBuilder.toString());
    }
    
    @Override
    public Analysis getAnalysisFromUrlWithActiveWarnings(String analysisUrl) throws IOException {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(analysisUrl);
            uriBuilder.addParameter("filter", UrlFilters.ACTIVE.getValue());
        } catch (URISyntaxException ex) {
            throw new AbortException(ex.getMessage());
        }
        
        return getAnalysisFromUrl(uriBuilder.toString());
    }
}
