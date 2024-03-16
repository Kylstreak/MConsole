package com.koolade446.mconsole.configs;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class LocalConfig extends HashMap<String, String> {
    private final File file;

    public LocalConfig(String location) {
        Path path = Path.of(location);
        this.file = path.toFile();

        if (file.exists()) {read();}
        else {create();}
    }

    private void read() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")){continue;}
                String[] splitString = line.split("=");
                this.put(splitString[0], splitString[1]);
            }
            reader.close();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            file.delete();
            create();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void create() {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            BufferedInputStream inStream = new BufferedInputStream(this.getClass().getClassLoader().getResourceAsStream("default-local-data.dat"));
            byte[] fileBytes = inStream.readAllBytes();
            inStream.close();

            OutputStream os = new FileOutputStream(file);
            os.write(fileBytes, 0, fileBytes.length);
            os.flush();
            os.close();

            read();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        try {
            ArrayList<String> lines = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            StringBuilder sb = new StringBuilder();
            for (String l : lines) {
                if (l.startsWith("#")) {
                    sb.append(l).append("\n");
                    continue;
                }
                String key = l.split("=")[0];
                if (this.containsKey(key)){
                    sb.append(String.format("%s=%s\n", key, this.remove(key)));
                }
            }
            if (!this.isEmpty()) {
                for (String k : this.keySet()) {
                    sb.append(String.format("%s=%s\n", k, this.remove(k)));
                }
            }

            byte[] fileBytes = sb.toString().getBytes();
            OutputStream os = new FileOutputStream(file);
            os.write(fileBytes, 0, fileBytes.length);
            os.flush();
            os.close();
            br.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
