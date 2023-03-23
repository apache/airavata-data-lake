package org.apache.airavata.datalake.service.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ProcessManager {


    public List<String> getUnavailableServices(List<String> fullListOfServices) throws IOException, InterruptedException {
        List<String> processes = getRunningProcesses();
        return fullListOfServices.stream().map(serviceName -> {
            AtomicBoolean notAvailable = new AtomicBoolean(true);
            processes.forEach(process->{
                if (process.trim().equals(serviceName.trim())){
                    notAvailable.set(false);
                }
            });
            if (notAvailable.get()){
                return  serviceName;
            }
            return null;
        }).collect(Collectors.toList());
    }


    public void startServices(List<String> commands) throws IOException, InterruptedException {
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");
        if (isWindows) {
            throw new UnsupportedOperationException();
        }
        Iterator<String> iterator = commands.iterator();
        while (iterator.hasNext()) {
            synchronized (this) {
                String command = iterator.next();
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("sh", "-c", command);
                Process process = processBuilder.start();
                process.waitFor();
                wait(30000);
            }
        }

    }


    private List<String> getRunningProcesses() throws IOException, InterruptedException {
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");
        if (isWindows) {
            throw new UnsupportedOperationException();
        }
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("sh", "-c", "jps");

        Process process = processBuilder.start();

        List<String> output = new ArrayList<>();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            String[] spaces = line.split(" ");
            if (spaces.length == 2) {
                output.add(spaces[1] + "\n");
            }
        }

        int exitVal = process.waitFor();
        if (exitVal != 0) {
            throw new RuntimeException("Command execution failed");
        }
        return output;
    }





}