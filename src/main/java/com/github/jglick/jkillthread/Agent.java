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

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Agent {

    public static void agentmain(String agentArgs) throws Exception {
        String[] splits = agentArgs.split("\\|");
        String tid = splits[0];
        String outFile = splits[1];

        try (PrintWriter out = new PrintWriter(new FileOutputStream(outFile))) {
            ProcessHandle handle = ProcessHandle.current();
            out.printf("Agent loaded  in %d %s%n", handle.pid(), handle.info().command().orElse(null));
            try {
                killThread(tid, out);
            } catch (Exception e) {
                e.printStackTrace();
                try (PrintWriter pw = new PrintWriter(new FileWriter(outFile))) {
                    e.printStackTrace(pw);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    e.addSuppressed(ex);
                }
                throw e;
            }
            out.println("Agent done.");
        }
    }

    private static void killThread(String tid, PrintWriter out) {
        ThreadGroup g = Thread.currentThread().getThreadGroup();
        while (true) {
            ThreadGroup p = g.getParent();
            if (p == null) {
                break;
            } else {
                g = p;
            }
        }
        Thread[] threads;
        int size = 256;
        while (true) {
            threads = new Thread[size];
            if (g.enumerate(threads) < size) {
                break;
            } else {
                size *= 2;
            }
        }
        boolean found = false;
        for (Thread thread : threads) {
            if (thread == null) {
                continue;
            }
            String name = thread.getName();
            if (name.contains(tid)) {
                out.printf("Killing \"%s\"%n", name);
                found = true;
                thread.interrupt();
                out.printf("Killed \"%s\"%n", name);
            }
        }
        if (!found) {
            out.printf("Did not find \"%s\"%n", tid);
        }
    }
}
