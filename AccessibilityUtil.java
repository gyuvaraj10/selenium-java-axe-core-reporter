package utils

import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import com.kms.katalon.core.util.KeywordUtil
import java.text.SimpleDateFormat
import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.webui.driver.DriverFactory

import com.deque.html.axecore.selenium.AxeBuilder;
import com.deque.html.axecore.selenium.AxeReporter;
import com.deque.html.axecore.selenium.ResultType;
import com.deque.html.axecore.axeargs.AxeRunOnlyOptions
import com.deque.html.axecore.axeargs.AxeRunOptions
import com.deque.html.axecore.results.CheckedNode
import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import static com.deque.html.axecore.selenium.AxeReporter.getReadableAxeResults;

import com.deque.html.axecore.selenium.*;
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject

import com.kms.katalon.core.checkpoint.Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testcase.TestCase
import com.kms.katalon.core.testdata.TestData
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows
import com.systemc.lcs.models.AxeResult

public class AccessibilityUtil {

	@Keyword
	def runAccessibility(String screenName) {
		def driver = DriverFactory.getWebDriver()
		Results results = new AxeBuilder().analyze(DriverFactory.getWebDriver())
		AxeResult aResult = new AxeResult()
		for (Rule violation: results.violations) {
			String border
			for(CheckedNode node: violation.nodes) {
				String selector =((ArrayList)node.target).get(0);
				border = getBorder(driver, selector)
				highlightElement(driver, selector);
			}
			aResult.base64Image = takeBase64Screenshot(driver)
			aResult.result = results
			aResult.screenName = screenName
		}
		return aResult
	}

	@Keyword
	def generateFullHTMLResport(List<AxeResult> results) {
		StringBuilder html = new StringBuilder("<html><head>");
		html.append("<meta charset='utf-8'>")
		html.append("<meta name='viewport' content='width=device-width, initial-scale=1'>")
		html.append("<title>Axe Accessibility Report</title>")
		html.append("<link href='https:\\\\cdn.jsdelivr.net\\npm\\bootstrap@5.3.3\\dist\\css\\bootstrap.min.css' rel='stylesheet' integrity='sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH' crossorigin='anonymous'>")
		html.append("<script src='https:\\\\cdn.jsdelivr.net\\npm\\bootstrap@5.3.3\\dist\\js\\bootstrap.bundle.min.js' integrity='sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz' crossorigin='anonymous'></script>")
		html.append("<script src='https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js'></script>")
		html.append("<script>")
		html.append("document.addEventListener('DOMContentLoaded', function() {")
		html.append("let tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle=\"tooltip\"]'));")
		html.append("let tooltipList = tooltipTriggerList.map(function(tooltipTriggerEl) {")
		html.append("return new bootstrap.Tooltip(tooltipTriggerEl, {")
		html.append("html: true // Enable HTML content")
		html.append("});")
		html.append("});")
		html.append("});")
		html.append("</script>")
		html.append("</head>")
		html.append("<body class='border border-success p-2 mb-2'>");
		html.append("<nav class='navbar' style='background-color:rgb(64, 146, 204);'>")
  		html.append("<div class='container-fluid d-flex justify-content-center'>")
    	html.append("<h1>Axe Accessibility Report</h1>")
		html.append("</nav></div>")
		html.append("<div class='container mt-4'><table class='table table-bordered align-middle'><thead class='table-light'><tr><th scope='col'>ScreenName</th><th scope='col'>Description</th><th scope='col'>Axe Rule Id</th><th scope='col'>Impact</th><th scope='col'>WCAG</th><th scope='col'>Count</th></tr></thead><tbody class='table-group-divider'>")
		int index=0
		for(AxeResult ar: results) {
			for(Rule rs: ar.result.violations) {
				html.append("<tr>")
				html.append("<td><a href='"+ar.result.url+"'>"+ar.screenName+"</a></td>")
				html.append("<td>"+rs.description)
				html.append("<i class='bi bi-image' data-bs-toggle='modal' data-bs-target='#imageModal"+index+"'> i</i>")
				html.append("<div class='modal fade' id='imageModal"+index+"' tabindex='-1' aria-labelledby='imageModalLabel"+index+"' aria-hidden='true'>")
        		html.append("<div class='modal-dialog modal-xl modal-dialog-scrollable'>")
                html.append("<div class='modal-content'>")
                html.append("<div class='modal-header'>")
                html.append("<h5 class='modal-title' id='imageModalLabel"+index+"'>Image Preview</h5>")
                html.append("<button type='button' class='btn-close' data-bs-dismiss='modal' aria-label='Close'></button>")
                html.append("</div>")
                html.append("<div class='modal-body text-center'>")
                html.append("<img src='data:image/jpeg;base64, "+ar.base64Image+"' class='img-fluid' alt='Preview Image'>")
                html.append("</div></div></div></div>")
				html.append("</td>")
				html.append("<td>"+rs.id+"</td>")
				html.append("<td>"+rs.impact+"</td>")
				html.append("<td>"+rs.tags+"</td>")
				html.append("<td>"+rs.nodes.size()+"</td>")
				html.append("</tr>")
				index++
			}
		}
		html.append("</tbody></table></div>")
		html.append("</body></html>");
		Files.write(Paths.get("axe-report.html"), html.toString().getBytes());
	}

	@Keyword
	def checkAccessibility() {
		def driver = DriverFactory.getWebDriver()
		AxeRunOnlyOptions runOnlyOptions = new AxeRunOnlyOptions()
		runOnlyOptions.setValues(null)
		AxeRunOptions options = new AxeRunOptions()
		options.setRules(null)
		Results results = new AxeBuilder().analyze(DriverFactory.getWebDriver())
		List<Rule> violations = results.getViolations()
		if(violations.size() == 0){
			KeywordUtil.logInfo("No Violation Found")
		}
		String axeReportPath = RunConfiguration.getReportFolder()+ File.separator
		String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new java.util.Date())
		String reportPath = axeReportPath + "axe-report"
		AxeReporter.writeResultsToJsonFile(reportPath,results)
		KeywordUtil.logInfo("Violation Report Path"+ reportPath)
		if(getReadableAxeResults(ResultType.Violations.getKey(),DriverFactory.getWebDriver(),violations) ){
			AxeReporter.writeResultsToTextFile(reportPath,
					AxeReporter.getAxeResultString())
		}
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode axeReport = objectMapper.readTree(new File(reportPath+".json"));
		List<String> screenshots = new ArrayList<>();
		Map<JsonNode, String> violationMap = new HashMap<>();
		for (JsonNode violation : axeReport.get("violations")) {
			for (JsonNode node : violation.get("nodes")) {
				String selector = node.get("target").get(0).asText();
				highlightElement(driver, selector);
			}
			violationMap.put(violation, takeScreenshot(driver))
		}
		generateHtmlReport(violationMap, axeReport);
	}

	private static void highlightElement(WebDriver driver, String selector) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("document.querySelector('" + selector + "').style.border='3px solid red'");
	}

	private static String getBorder(WebDriver driver, String selector) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		return js.executeScript("return document.querySelector('" + selector + "').style.border;");
	}

	private static String takeScreenshot(WebDriver driver) throws IOException {
		File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE)
		String path = "screenshot" + System.currentTimeMillis() + ".png"
		File destFile = new File(path);
		Files.createDirectories(Paths.get("screenshots"));
		Files.copy(srcFile.toPath(), destFile.toPath());
		return path;
	}

	private static String takeBase64Screenshot(WebDriver driver) throws IOException {
		return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64)
	}

	private static void generateHtmlReport(Map<JsonNode, String> violationMap, JsonNode rootNode) throws IOException {
		StringBuilder html = new StringBuilder("<html><head>");
		html.append("<meta charset='utf-8'>")
		html.append("<meta name='viewport' content='width=device-width, initial-scale=1'>")
		html.append("<title>Axe Accessibility Report</title>")
		html.append("<link href='https:\\\\cdn.jsdelivr.net\\npm\\bootstrap@5.3.3\\dist\\css\\bootstrap.min.css' rel='stylesheet' integrity='sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH' crossorigin='anonymous'>")
		html.append("<script src='https:\\\\cdn.jsdelivr.net\\npm\\bootstrap@5.3.3\\dist\\js\\bootstrap.bundle.min.js' integrity='sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz' crossorigin='anonymous'></script>")
		html.append("</head>")
		html.append("<body><h1>Axe Accessibility Report</h1>");
		html.append("<div class='alert alert-success' role='alert'>Url: "+rootNode.get("url")+"</div>")
		html.append("<p>Axe Core found <span class='badge text-bg-danger'>"+rootNode.get("violations").size()+"</span> violations")
		html.append("<table class='table'><thead><tr><th scope='col'>Description</th><th scope='col'>Axe Rule Id</th><th scope='col'>Impact</th><th scope='col'>WCAG</th><th scope='col'>Count</th></tr><thead><tbody>")
		for (Map.Entry<JsonNode, String> violationEntry : violationMap.entrySet()) {
			def node = violationEntry.key
			html.append("<tr>")
			html.append("<td>"+node.get("description")+"</td>")
			html.append("<td>"+node.get("id")+"</td>")
			html.append("<td>"+node.get("impact")+"</td>")
			html.append("<td>"+node.get("tags")+"</td>")
			html.append("<td>"+node.get("nodes").size()+"</td>")
			html.append("</tr>")
		}
		html.append("<tbody>")
		html.append("</body></html>");
		Files.write(Paths.get("axe-report.html"), html.toString().getBytes());
	}
}
