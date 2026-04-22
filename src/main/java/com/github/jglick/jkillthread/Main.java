/*
 * Copyright 2013 Jesse Glick.
 *
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
 */

package com.github.jglick.jkillthread;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage:");
            System.err.println("  java -jar jkillthread.jar <PID> <TID>");
            System.err.println("    where <PID> is as in jps, or some substring of the text after a PID visible in jps -lm");
            System.err.println("    and <TID> is a thread name (like pool-9-thread-1), or substring (like pool-)");
            System.err.println("Requires JDK 6+ for both this tool and the target VM.");
            System.exit(2);
        }
        String vmid = args[0];
        String tid = args[1];
        File self = new File(URI.create(Main.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm()));
        File outFile = File.createTempFile("jkillthread-out-", ".txt");
        outFile.deleteOnExit();

        List<VirtualMachineDescriptor> matches = VirtualMachine.list().stream()
                .filter(desc -> !desc.displayName().contains("jkillthread"))
                .filter(desc -> desc.displayName().contains(vmid) || vmid.equals(desc.id()))
                .collect(Collectors.toList());
        if (matches.isEmpty()) {
            System.err.println("No Java processes found matching '" + vmid + "'");
            System.exit(1);
        }
        for (VirtualMachineDescriptor match : matches) {
            VirtualMachine m = VirtualMachine.attach(match);
            try {
                String agentArgs = tid + "|" + outFile.getAbsolutePath();
                System.out.printf("Agent loading in %s %s%n", match.id(), match.displayName());
                m.loadAgent(self.getAbsolutePath(), agentArgs);
                printOutput(outFile);
            } catch (Exception ex) {
                System.err.printf("Failed to attach to %s %s%n", match.id(), match.displayName());
                ex.printStackTrace();
                System.err.println();
            } finally {
                m.detach();
            }
        }
    }

    private static void printOutput(File outFile) throws IOException {
        Path errorFilePath = outFile.toPath();
        if (outFile.length() > 0) {
            String errorContent = Files.readString(errorFilePath);
            System.out.print(errorContent);
            Files.newOutputStream(errorFilePath, StandardOpenOption.TRUNCATE_EXISTING).close();
        }
    }
}
