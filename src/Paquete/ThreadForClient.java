package Paquete;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ThreadForClient extends Thread {

    private Socket s;
    public File currentDir;

    public ThreadForClient(Socket s) {
        this.s = s;
    }

    public void run() {
        currentDir = new File("/ftp/data/");
        if (!currentDir.exists()) {
            currentDir.mkdirs();
        }
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream(), true);
            String fromClient = null;
            out.println("ok\r\n");
            out.flush();
            out.println("220 localhost connected\r\n");
            out.println("331 Anonym no password needed\r\n");
            out.println("211-Features:");
            out.println("MDTM");
            out.println("REST STREAM");
            out.println("SIZE");
            out.println("MLST type*;size*;modify*;");
            out.println("MLSD");
            out.println("UTF8");
            out.println("CLNT");
            out.println("MFMT");
            out.println("211 End");
            out.println("230 OK. Current directory is /");
            boolean connected = true;
            while (connected) {
                fromClient = in.readLine();
                System.out.println("From client: " + fromClient);
                if (fromClient != null) {
                    if (fromClient.equals("BYE")) {
                        out.println("Bye Client\r\n");
                        connected = false;
                    } else {
                        processCommand(fromClient, out);
                    }
                }
            }
        } catch (IOException e) {
            String msg = e.getMessage();
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (s != null) {
                    s.close();
                }
            } catch (IOException e) {
                String msg = e.getMessage();
                e.printStackTrace();
            }
        }
    }

    private void processCommand(String command, PrintWriter out) {

        String[] args = command.split(" ");

        switch (args[0].toUpperCase()) {

            case "MKDIR":
                createDirectory(out, args[1]);
                break;
            case "PWD":
                currentDirectory(out);
                break;
            case "LS":
                listFiles(out);
                break;
            case "CD":
                changeDirectory(out, args[1]);
                break;
            case "DELETE":
                deleteFile(out, args[1]);
                break;
            case "GET":
                getFile(out, args[1]);
                break;
            default:
                break;
        }

    }

    private void createDirectory(PrintWriter out, String directoryName) {
        File theDir = new File(currentDir.getPath() + "\\" + directoryName);

        // if the directory does not exist, create it
        if (!theDir.exists()) {
            System.out.println("creating directory: " + directoryName);
            boolean result = theDir.mkdir();

            if (result) {
                System.out.println("DIR created");
                out.println(directoryName + " has been created\r\n");
                out.flush();
            }
        }
    }

    private void currentDirectory(PrintWriter out) {
        out.println(currentDir.getAbsolutePath() + "\r\n");
        out.flush();
    }

    private void listFiles(PrintWriter out) {
        String strLista = "";
        for (int i = 0; i < currentDir.list().length; i++) {
            strLista += currentDir.list()[i] + "\r\n";
        }
        if (currentDir.list().length == 0) {
            out.println("Empty directory\r\n");
        } else {
            out.println(strLista + "\r\n");
        }
        out.flush();
    }

    private void changeDirectory(PrintWriter out, String dirName) {
        String[] lst = currentDir.list();
        boolean encontro = false;
        for (int i = 0; i < lst.length; i++) {//ciclo para buscar la carpeta en el directorio
            if (lst[i].equals(dirName)) {
                encontro = true;
                break;
            }
        }
        if (currentDir.isDirectory()) {
            if (dirName.equals("..")) {
                currentDir = new File(currentDir.getParent());
            } else if (encontro) {
                currentDir = new File(currentDir.getPath() + "\\" + dirName + "\\");//posicionarse en la carpeta destino
            } else {
                out.println("\tDirectory: " + dirName + "\\ not found\r\n");// para saber si encontro
            }
        } else {
            out.println(dirName + " is not a directory\r\n");// cuando no es un directorio
        }
        out.println("Changed to " + currentDir.getAbsolutePath() + "\r\n");
        out.flush();
    }

    private void deleteFile(PrintWriter out, String fileName) {
        String[] lst = currentDir.list();
        boolean encontro = false;
        for (int i = 0; i < lst.length; i++) {//ciclo para buscar la carpeta en el directorio
            if (lst[i].equals(fileName)) {
                encontro = true;
                break;
            }
        }
        if (currentDir.isDirectory()) {//si es una carpeta
            if (encontro) {
                currentDir = new File(currentDir.getPath() + "\\" + fileName + "\\");//colocarse en la carpeta a eliminar
                if (currentDir.delete()) {//si se pudo borrar                        
                } else {
                    borrarDirectorio(currentDir, out);//ir al metodo para eliminar directorios interiores
                    currentDir.delete();//eliminar la carpeta ya vacia
                }
                currentDir = new File(currentDir.getParent());//colocarse en la carpeta padre de la carpeta eliminada
                out.println("\tDirectory " + fileName + "\\ has been deleted\r\n");
            } else {
                out.println("\tDirectory: " + fileName + "\\ not found\r\n");// para saber si encontro
            }
            out.flush();
        }
    }

    private void getFile(PrintWriter out, String fileName) {
    }

    //recursivo para eliminar contenido interior de una carpeta
    public void borrarDirectorio(File directorio, PrintWriter out) {
        File[] ficheros = directorio.listFiles();
        for (int x = 0; x < ficheros.length; x++) {
            if (ficheros[x].isDirectory()) {
                borrarDirectorio(ficheros[x], out);
            }
            String tempName = ficheros[x].getName();
            ficheros[x].delete();
            out.println(tempName + " has been deleted\r\n");
        }
    }

}
