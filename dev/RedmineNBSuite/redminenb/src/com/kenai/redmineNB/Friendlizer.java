package com.kenai.redmineNB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.OnStart;


/**
 * Make the Issue Tracker module friendly with RedmineNB.
 *
 * @author quangtin3 <quanghx2@viettel.com.vn>
 * @author Anchialas <anchialas@gmail.com>
 * @see https://github.com/quangtin3/module-friendlizer
 */
public final class Friendlizer {

   private static final Logger LOG = Logger.getLogger(Friendlizer.class.getName());
   //
   // Internal Manifest file key words
   //
   private static final String REDMINENB_CODE_NAME_BASE = Friendlizer.class.getPackage().getName();
   private static final String NETBEANS_MODULE_FRIEND_KEY = "OpenIDE-Module-Friends";
   private static final int BUFFER_SIZE = 4096 * 2;
   private static final String META_INF_MANIFESTM_ENTRY = "META-INF/MANIFEST.MF";
   private static final String SUBFIX_VEP_TOOL_TEMPLATE_ZIP_FILE = "_vep_tool_template.zip";


   @OnStart
   public static class Installer implements Runnable {

      @Override
      public void run() {
         // "org-netbeans-modules-bugtracking.jar"
         File module = InstalledFileLocator.getDefault().locate("modules/org-netbeans-modules-bugtracking.jar", 
                                                                "org.netbeans.modules.bugtracking", false);
         patchModules(module);
      }

   }

   /**
    * Patches the specified modules.
    *
    * @param netbeansPath
    * @param modules private modules to patch
    *
    * @return
    */
   public static boolean patchModules(File... modules) {
      LOG.log(Level.INFO, "Module Friendlizer started");

      for (File module : modules) {
         LOG.log(Level.INFO, "Examining module {0}", module);

         if (module.isFile()) {
            if (!patchingModuleJarFile(module)) {
               return false;
            }
         } else {
            LOG.log(Level.INFO, "Not a file");
         }
      }
      return true;
   }

   /**
    * Replace file
    *
    * @param targetFile target path
    * @param newFile new file path
    */
   private static boolean replaceFile(File targetFile, File newFile) {
      if (targetFile.exists()
              && targetFile.isFile()
              && newFile.exists()
              && newFile.canRead()) {

         targetFile.delete();
         try {
            LOG.log(Level.FINE, "start replace");
            targetFile.createNewFile();
            return newFile.renameTo(targetFile);

         } catch (IOException e) {
            LOG.log(Level.WARNING, "replace error: {0}", e.getMessage());
            return false;
         }
      } else {
         LOG.log(Level.INFO, "invalid input");
         return false;
      }
   }

   /**
    * Patching module jar file procedure. For technique detail see:
    * http://javahowto.blogspot.com/2011/07/how-to-programmatically-copy-jar-files.html
    *
    * @param filename jar file to patch
    *
    * @throws IOException error occur
    */
   private static boolean patchingModuleJarFile(File module) {

      File temporatyFile = new File(module.getAbsolutePath() + SUBFIX_VEP_TOOL_TEMPLATE_ZIP_FILE);

      /* Start make new temporary file */
      JarFile jarfile = null;
      boolean ret;
      try {
         jarfile = new JarFile(module);
         if (jarfile.getManifest() == null) {
            LOG.log(Level.FINE, "Just ignore file without Manifest: {0}", module);
            return true;
         }
         if (updateManifestFriendList(jarfile.getManifest().getMainAttributes())) {
            ret = copyAllJarEntries(jarfile, temporatyFile);
         } else {
            LOG.log(Level.INFO, "Nothing patched for: {0}", module);
            return true;
         }
      } catch (IOException e) {
         ret = false;
         LOG.log(Level.WARNING, "Create temporaty file error: {0}", e.getMessage());
      } finally {
         if (jarfile != null) {
            try {
               jarfile.close();
            } catch (IOException e) { // NOPMD
               // ignored
            }
         }
      }

      /* If we have new file, using it to replace the old one */
      if (ret) {
         LOG.log(Level.INFO, "Replace old file");
         if (replaceFile(module, temporatyFile)) {
            LOG.log(Level.INFO, "Replace successful!");
            return true;
         } else {
            LOG.log(Level.WARNING, "Can''t replace file {0} with file {1}", new Object[]{module, temporatyFile});
         }
      } else {
         LOG.log(Level.INFO, "No temporary file created.");
      }
      return false;
   }

   /**
    * Copy all jarFile entries to new file
    *
    * @param jarfile
    * @param newFile
    *
    * @return
    *
    * @throws IOException
    */
   private static boolean copyAllJarEntries(JarFile jarfile, File newFile) {
      byte[] buffer = new byte[BUFFER_SIZE];

      FileOutputStream fos = null;
      JarOutputStream jaros = null;
      try {
         fos = new FileOutputStream(newFile);
         jaros = new JarOutputStream(fos, jarfile.getManifest());

         Enumeration<JarEntry> jarEntries = jarfile.entries();
         while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();

            // Copy bug ignore manifest entry
            if (!jarEntry.getName().equalsIgnoreCase(META_INF_MANIFESTM_ENTRY)) {

               InputStream entryIs = jarfile.getInputStream(jarEntry);
               jaros.putNextEntry(new JarEntry(jarEntry.getName()));

               int bytesRead;
               while ((bytesRead = entryIs.read(buffer)) != -1) {
                  jaros.write(buffer, 0, bytesRead);
               }
               entryIs.close();
               jaros.flush();
               jaros.closeEntry();
            }
         }

         jaros.flush();
         jaros.close();
         fos.close();
         return true;

      } catch (IOException e) {
         LOG.log(Level.INFO, "Error while copying jarEntries to file {0}: {1}", new Object[]{newFile, e.getMessage()});
         return false;

      } finally {
         if (jaros != null) {
            try {
               jaros.close();
            } catch (IOException e) { // NOPMD
               // ignored
            }
         }
         if (fos != null) {
            try {
               fos.close();
            } catch (IOException e) { // NOPMD
               // ignored
            }
         }
      }
   }

   /**
    * Put RedmineNB module to friend list
    *
    * @param oldFriendList old friend list string
    *
    * @return new friend list string
    */
   private static String makeFriend(String oldFriendList) {
      if (oldFriendList == null || oldFriendList.isEmpty()) {
         return REDMINENB_CODE_NAME_BASE;
      }

      if (oldFriendList.contains(REDMINENB_CODE_NAME_BASE)) {
         return oldFriendList;
      } else {
         String newFriendList = oldFriendList.trim();
         if (newFriendList.endsWith(",")) {
            newFriendList += " " + REDMINENB_CODE_NAME_BASE;
         } else {
            newFriendList += ", " + REDMINENB_CODE_NAME_BASE;
         }
         return newFriendList;
      }
   }

   /**
    * Update manifest attributes
    *
    * @param att Manifest Main Attributes
    *
    * @return true if the Attributes need to modified (already modified)
    */
   private static boolean updateManifestFriendList(Attributes att) {
      for (Object key : att.keySet()) {

         if (key instanceof Attributes.Name) {
            Attributes.Name moduleKey = (Attributes.Name) key;

            // Looking for friends key
            if (moduleKey.toString().equalsIgnoreCase(NETBEANS_MODULE_FRIEND_KEY)) {
               Object value = att.get(key);
               if (value instanceof String) {

                  // Get old friend list
                  String oldFriendList = (String) value;

                  // Make new friend list
                  String newFriendList = makeFriend(oldFriendList);

                  // Put new list in-place
                  att.put(key, newFriendList);

                  return true;
               }
            }
         }
      }

      return false;
   }

}
