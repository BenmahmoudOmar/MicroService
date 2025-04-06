package tn.esprit.Controller;

import org.json.JSONException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StreamUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/anomaly")
public class AnomalyController {

    // Path to the Python script
    @Value("${python.script.path}")
    private String pythonScriptPath;

    // Path to the model pickle file
    @Value("${python.model.path}")
    private String modelPath;

    @PostMapping("/predict")
    public ResponseEntity<String> predictAnomaly() {
        try {
            // Call the Python script (no input JSON as it's always the same)
            ProcessBuilder processBuilder = new ProcessBuilder("C:\\\\Users\\\\Dell\\\\Desktop\\\\User-Service\\\\venv\\\\Scripts\\\\python.exe", pythonScriptPath, modelPath);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // Capture the output from the Python script
            InputStream inputStream = process.getInputStream();
            String result = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

            // Log the output to verify it
            System.out.println("Python script output: " + result);

            // Wait for the Python script to finish execution
            process.waitFor();



            // Check if the result is a valid JSON array
            try {
                // Parse the result into a JSON array
                JSONArray jsonArray = new JSONArray(result);

                // Find the last row where anomaly == 1
                JSONObject lastRow = jsonArray.getJSONObject(jsonArray.length() - 1);

// Return the last row (no anomaly check)
                return ResponseEntity.ok(lastRow.toString());

                // If no anomaly found, return a message

            } catch (JSONException e) {
                // Handle JSON parsing error
                System.err.println("Invalid JSON returned from Python script: " + result);
                return ResponseEntity.status(500).body("{\"message\": \"Error: Invalid JSON returned from Python script\"}");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"message\": \"An error occurred while processing the anomaly detection.\"}");
        }
    }

}
