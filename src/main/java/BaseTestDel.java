import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

import java.io.*;
import java.util.List;
import java.util.Properties;


public class BaseTestDel {
    public static Properties locators;
    public static WebDriver driver;
    public static String TotalPages;
    public static int count;
    public static boolean last_isFIle_empty;

    public static void main(String[] args) throws IOException, InterruptedException {
        count = 0;
        basePage();
        previousYearLoop();
        System.out.println("Total Previous buttons found:" + count);
    }

    public static void basePage() throws IOException, InterruptedException {
        BufferedReader rdr = new BufferedReader(new FileReader("locators.properties"));
        locators = new Properties();
        locators.load(rdr);

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://esearch.delhigovt.nic.in/Complete_search_without_regyear.aspx");

        Select obj = new Select(driver.findElement(By.xpath(locators.getProperty("SRO"))));
        obj.selectByVisibleText("South East-Mehrauli (SR V)");
        Thread.sleep(3000);
        Select obj1 = new Select(driver.findElement(By.xpath(locators.getProperty("Locality"))));
//        driver.findElement(By.xpath(locators.getProperty("Locality"))).click();
        obj1.selectByValue("0030");
        Thread.sleep(13000);
        driver.findElement(By.xpath(locators.getProperty("search"))).click();
    }

    public static void flagTypePage() throws InterruptedException {
        WebElement table = driver.findElement(By.xpath(".//table[@id='ctl00_ContentPlaceHolder1_gv_search']"));
        // Find all rows within the table
        Thread.sleep(1000);
        List<WebElement> rows = table.findElements(By.xpath("tbody/tr"));
            for (int i = rows.size() - 2; i < rows.size(); i++) {
            WebElement row = rows.get(i);
            List<WebElement> last_cell_values = row.findElements(By.xpath("td"));

//          only for flag
            for (WebElement cell : last_cell_values) {
                String cellText = cell.getText().trim();
                if (cellText.contains("Show rows:")) {
                    last_isFIle_empty = true;
//                    System.out.println("Contains Show rows " + last_isFIle_empty);
                }
                else{
                    last_isFIle_empty = false;
                }
            }
        }
    }

    public static void fetchData() throws IOException, InterruptedException {
        int x;
        WebElement table = driver.findElement(By.xpath(".//table[@id='ctl00_ContentPlaceHolder1_gv_search']"));
        // Find all rows within the table
        List<WebElement> rows = table.findElements(By.xpath("tbody/tr"));
        // FileWriter for CSV output
        try (FileWriter csvWriter = new FileWriter("table_data.csv", true)) {
            // Check if file is empty to determine if we need to write the header
            flagTypePage();
            boolean isFileEmpty = new File("table_data.csv").length() == 0;
            // Iterate over each row, skipping the last two rows
            if (last_isFIle_empty==true) {
                 x = 2;
            }
            else{
                 x = 1;
            }
            // Iterate over each row, skipping the last two rows / one row
            for (int i = 0; i < rows.size() - x; i++) {
                WebElement row = rows.get(i);
                List<WebElement> cells = row.findElements(By.xpath("td"));
                StringBuilder csvLine = new StringBuilder();

                // Iterate over each cell in the row
                for (WebElement cell : cells) {
                    String cellText = cell.getText().trim();
                    // Handle cases where the cell text contains commas
                    if (cellText.contains(",")) {
                        cellText = "\"" + cellText + "\"";
                    }
                    csvLine.append(cellText).append(",");
                }

                // Write the CSV line to file
                csvWriter.append(csvLine.toString().replaceAll(",$", "")).append("\n");
            }
            // Flush and close the CSV writer
            csvWriter.flush();
            csvWriter.close();

            System.out.println("CSV file has been written successfully.");
            Thread.sleep(1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sheetLoop() {
        try {
//        GET NO OF SHEET
            Thread.sleep(1000);
            // Initial fetch
            System.out.println("User fetches all data from page: 1");
            fetchData();
//            System.out.println("flag last page " + last_isFIle_empty);
            if (last_isFIle_empty) {
            TotalPages = driver.findElement(By.xpath(locators.getProperty("TotalPages"))).getText().trim();
            System.out.println("total sheets: " + TotalPages);
            // Loop to click "Previous Year" button and fetch data again
            for (int i = 2; i <= Integer.parseInt(TotalPages); i++) {
                System.out.println("User fetches all data from page: " + i);

                WebElement nextSheet = driver.findElement(By.xpath(locators.getProperty("nextSheet")));
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView();", nextSheet);
                Thread.sleep(1000);
                nextSheet.click();
                Thread.sleep(3000);
                fetchData();
            }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void previousYearLoop() {
        while (true) {
            try {
                // Initial fetch
                last_isFIle_empty = false;
                sheetLoop();
                // Loop to click "Previous Year" button and fetch data again
                Thread.sleep(1000);
                if (last_isFIle_empty) {
                    WebElement showRowTextInput = driver.findElement(By.xpath(locators.getProperty("showRowTextInput")));
                    Thread.sleep(1000);
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView();", showRowTextInput);
                    Thread.sleep(1000);
                    showRowTextInput.click();
                    Thread.sleep(500);
                    showRowTextInput.clear();
                    Thread.sleep(1000);
                    System.out.println("Clear show row text=======================");
                    WebElement temp = driver.findElement(By.xpath(locators.getProperty("showRowTextInput")));
//                    temp.sendKeys("1");
//                    System.out.println("Sent keys");
//                    Thread.sleep(1000);
                    temp.sendKeys(Keys.ENTER);
                    Thread.sleep(3000);
                    WebElement temp1 = driver.findElement(By.xpath(locators.getProperty("showRowTextInput")));
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView();", temp1);
                }
                WebElement previousYear = driver.findElement(By.xpath(locators.getProperty("previousYear")));
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView();", previousYear);
                Thread.sleep(3000);
                previousYear.click();
                count++;
                System.out.println("Previous year click counter: "+count);
                Thread.sleep(3000);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Last previous button found. Stopping..");
                break;
            }
        }
    }


}
