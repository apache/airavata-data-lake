package org.apache.airavata.datalake.service.monitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ServiceMonitor {

    public static void main(String[] args) throws IOException, InterruptedException {


        if (args.length == 0) {
            throw new RuntimeException("please give a list of names to monitor", null);
        }

        ProcessManager processManager = new ProcessManager();
        Map<String, String> fileMap = parseFile(args[0]);

        List<String> unavailableServices = processManager.getUnavailableServices(new ArrayList<String>(fileMap.keySet()));

        MessageSender messageSender = new MessageSender(args[1]);

        for (String service : unavailableServices) {
            if (service != null && !service.isEmpty()) {
                messageSender.sendMessage(service, MessageSender.SERVICE_STATUS.STOPPED);
            }
        }

        List<String> servicesToBeStarted = unavailableServices.stream().map(service -> {
            if (service != null && !service.isEmpty()) {
                return fileMap.get(service);
            }
            return null;
        }).collect(Collectors.toList());

        processManager.startServices(servicesToBeStarted);

        List<String> failedServices = processManager.getUnavailableServices(new ArrayList<String>(fileMap.keySet()));
        List<String> listWithoutNulls = failedServices.parallelStream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (listWithoutNulls.isEmpty()) {
            for (String service : unavailableServices) {
                if (service != null && !service.isEmpty()) {
                    messageSender.sendMessage(service, MessageSender.SERVICE_STATUS.STARTED);
                }
            }
        }
    }


    private static Map<String, String> parseFile(String path) throws IOException {
        Map<String, String> fileMap = new HashMap<>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileInputStream(path));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] commands = line.split(":");
                fileMap.put(commands[0].trim(), commands[1].trim());
            }
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return fileMap;
    }
}
