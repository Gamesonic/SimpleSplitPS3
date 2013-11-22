package simplesplitps3;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.TreeSet;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class SimpleSplitPS3
{
    private JFrame jframe;
    private GridBagConstraints c;
    private ButtonGroup group;
    private JRadioButton join;
    private JRadioButton split;
    private JCheckBox delete;
    private JCheckBox rename;
    private JCheckBox change;
    private JCheckBox recreate;
    private JButton search;
    private JLabel filesFound;
    private JLabel info;
    private JProgressBar fileProgress;
    private JProgressBar totalProgress;
    private JFileChooser searcher;
    private Path destination;
    private final long MAX_SIZE = 4294967295L;
    private final long CHUNK_SIZE = 1431655765L;
    private final double VERSION = 1.0;
    private final String TITLE = "SimpleSplitPS3 v" + VERSION;
    private final String EXTENSION_PREFIX = ".666";
    private final String EXTENSION_TEXT = ".666##";

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                new SimpleSplitPS3().createAndShowGUI();
            }//run
        });//invokeLater
    }//main

    private SimpleSplitPS3()
    {
        jframe = new JFrame(TITLE);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setResizable(false);
        jframe.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        colorGrayAndWhite();
    }//constructor

    private void createAndShowGUI()
    {
        join = new JRadioButton("Join");
        split = new JRadioButton("Split");
        delete = new JCheckBox("Delete");
        rename = new JCheckBox("Rename");
        change = new JCheckBox("Change");
        recreate = new JCheckBox("Recreate");
        filesFound = new JLabel(" ", null, JLabel.CENTER);
        info = new JLabel(":: Developed by Dermy ::", null, JLabel.CENTER);
        fileProgress = new JProgressBar();
        totalProgress = new JProgressBar();

        search = new JButton("Search For Files");
        search.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                searcher = new JFileChooser();
                searcher.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                searcher.setDialogTitle("Search A Directory Recursively");
                searcher.setApproveButtonText("Search");

                int option = searcher.showOpenDialog(jframe);

                if(option == JFileChooser.APPROVE_OPTION)
                {
                    ArrayList<TreeSet<Path>> treeList =
                            findFiles(searcher.getSelectedFile().toPath());

                    if(treeList.get(0).isEmpty())
                    {
                        String message = "";

                        if(join.isSelected())
                        {
                            message = "No " + EXTENSION_TEXT + " files found.";
                        }//if

                        else if(split.isSelected())
                        {
                            message = "No files greater than or equal to 4 "
                                    + "GiB found.";
                        }//else if

                        JOptionPane.showMessageDialog(jframe, message, TITLE,
                                JOptionPane.PLAIN_MESSAGE);
                    }//if

                    else
                    {
                        if(change.isSelected())
                        {
                            if(!changeDestination())
                            {
                                searcher = null;
                            }//if
                        }//if

                        if(searcher != null)
                        {
                            new Task(treeList).execute();
                        }//if
                    }//else
                }//if
            }//actionPerformed
        });//addActionListener

        join.setFocusable(false);
        split.setFocusable(false);
        delete.setFocusable(false);
        rename.setFocusable(false);
        change.setFocusable(false);
        recreate.setFocusable(false);
        search.setFocusable(false);
        filesFound.setFocusable(false);
        info.setFocusable(false);
        fileProgress.setFocusable(false);
        totalProgress.setFocusable(false);
        fileProgress.setStringPainted(true);
        totalProgress.setStringPainted(true);
        fileProgress.setString("File Progress");
        totalProgress.setString("Total Progress");

        join.setSelected(true);
        delete.setSelected(true);
        rename.setSelected(true);

        group = new ButtonGroup();
        group.add(split);
        group.add(join);

        c.fill = GridBagConstraints.BOTH;
        c.gridy = 0;
        jframe.add(join, c);
        c.gridy = 1;
        jframe.add(split, c);
        c.gridy = 2;
        jframe.add(delete, c);
        c.gridy = 3;
        jframe.add(rename, c);
        c.gridy = 4;
        jframe.add(change, c);
        c.gridy = 5;
        jframe.add(recreate, c);
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 6;
        c.ipadx = 45;
        jframe.add(search, c);
        c.gridx = 0;
        c.gridy = 6;
        c.gridheight = 1;
        c.gridwidth = 2;
        c.ipadx = 0;
        jframe.add(filesFound, c);
        c.gridy = 7;
        jframe.add(info, c);
        c.gridy = 8;
        jframe.add(fileProgress, c);
        c.gridy = 9;
        jframe.add(totalProgress, c);
        jframe.pack();

        jframe.setLocationRelativeTo(null);
        jframe.setVisible(true);
    }//createAndShowGUI

    private void colorGrayAndWhite()
    {
        try
        {
            UIManager.setLookAndFeel(
                    "javax.swing.plaf.nimbus.NimbusLookAndFeel");
            jframe.getContentPane().setBackground(Color.DARK_GRAY);
            UIManager.put("nimbusBase", Color.DARK_GRAY);
            UIManager.put("nimbusBlueGrey", Color.DARK_GRAY);
            UIManager.put("nimbusSelectionBackground", Color.DARK_GRAY);
            UIManager.put("nimbusFocus", Color.WHITE);
            UIManager.put("text", Color.WHITE);
            UIManager.put("control", Color.DARK_GRAY);
            UIManager.put("List[Selected].textForeground", Color.WHITE);
            UIManager.put("nimbusLightBackground", Color.DARK_GRAY);
            UIManager.put("nimbusBase", Color.DARK_GRAY);
        }//try

        catch(ClassNotFoundException | InstantiationException
                | IllegalAccessException |
                UnsupportedLookAndFeelException ex){}//catch
    }//colorGrayAndWhite

    private ArrayList<TreeSet<Path>> findFiles(Path directory)
    {
        final ArrayList<TreeSet<Path>> treeList = new ArrayList<>(2);
        final TreeSet<Path> filesToSplitOrJoin = new TreeSet<>();
        final TreeSet<Path> foldersToRename = new TreeSet<>();

        try
        {
            EnumSet<FileVisitOption> opts = EnumSet.of(
                    FileVisitOption.FOLLOW_LINKS);

            Files.walkFileTree(directory, opts, Integer.MAX_VALUE,
                    new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path path,
                BasicFileAttributes attrs) throws IOException
                {
                    if(attrs.isRegularFile())
                    {
                        if(join.isSelected())
                        {
                            if(path.getFileName().toString().contains(
                                EXTENSION_PREFIX))
                            {
                                Path parent;
                                filesToSplitOrJoin.add(path);

                                if((parent = findParent(path)) != null)
                                {
                                    foldersToRename.add(parent);
                                }//if
                            }//if
                        }//if

                        else if(split.isSelected())
                        {
                            if(Files.size(path) > MAX_SIZE)
                            {
                                Path parent;
                                filesToSplitOrJoin.add(path);

                                if((parent = findParent(path)) != null)
                                {
                                    foldersToRename.add(parent);
                                }//if
                            }//if
                        }//else if
                    }//if

                    return FileVisitResult.CONTINUE;
                }//visitFile
            });//walkFileTree
        }//try

        catch(IOException ex)
        {
            quit("Error occurred while searching for files.");
        }//catch

        treeList.add(filesToSplitOrJoin);
        treeList.add(foldersToRename);

        return treeList;
    }//findFiles

    private Path findParent(Path filePath)
    {
        int namePos = filePath.getNameCount() - 2;

        while(namePos >= 0)
        {
            if(filePath.getName(namePos).toString().equals("PS3_GAME"))
            {
                return Paths.get(File.separator + filePath.subpath(0,
                        namePos).toString());
            }//if

            else
            {
                namePos--;
            }//else
        }//while

        return null;
    }//findParent

    private boolean changeDestination()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Choose A Destination Directory");
        chooser.setApproveButtonText("Choose");

        int option = chooser.showOpenDialog(jframe);

        if(option == JFileChooser.APPROVE_OPTION)
        {
            destination = chooser.getSelectedFile().toPath();
            return true;
        }//if

        return false;
    }//changeDestination

    private void join(ArrayList<TreeSet<Path>> treeList)
    {
        int filesJoined = 0;
        filesFound.setText(treeList.get(0).size() + " " + EXTENSION_TEXT +
            " File(s) Found");

        ArrayList<TreeSet<Path>> subsets = makeSubsets(treeList.get(0));
        String filename;

        for(TreeSet<Path> filePieces : subsets)
        {
            fileProgress.setValue(0);
            filename = filePieces.first().getFileName().toString();
            info.setText("Joining " + filename.substring(0,
                    filename.length() - 6));
            jframe.pack();

            Path destinationFolderToRename;

            if((destinationFolderToRename =
                    joinFiles(filePieces, treeList.get(0))) != null)
            {
                treeList.get(1).add(destinationFolderToRename);
            }//if

            totalProgress.setValue((int) (((double) ++filesJoined /
                    (double) subsets.size()) * 100.0));
        }//for
    }//join

    private void split(ArrayList<TreeSet<Path>> treeList)
    {
        int filesSplit = 0;
        filesFound.setText(treeList.get(0).size() + " Large File(s) Found");

        for(Path largeFile : treeList.get(0))
        {
            fileProgress.setValue(0);
            info.setText("Splitting " + largeFile.getFileName().toString());
            jframe.pack();

            Path destinationFolderToRename;

            if((destinationFolderToRename = splitFile(largeFile)) != null)
            {
                treeList.get(1).add(destinationFolderToRename);
            }//if

            totalProgress.setValue((int) (((double) ++filesSplit /
                    (double) treeList.get(0).size()) * 100.0));
        }//for
    }//split

    private ArrayList<TreeSet<Path>> makeSubsets(TreeSet<Path> splitFilesSet)
    {
        ArrayList<TreeSet<Path>> subsets = new ArrayList<>();

        while(!splitFilesSet.isEmpty())
        {
            TreeSet<Path> filePieces = new TreeSet<>();
            Path splitFilePath = splitFilesSet.pollLast();
            String[] splitName = splitFilePath.getFileName().toString().
                    split(EXTENSION_PREFIX);
            int piecesLeft = Integer.parseInt(splitName[1]);

            filePieces.add(splitFilePath);

            for(int i = 0; i < piecesLeft; i++)
            {
                filePieces.add(splitFilesSet.pollLast());
            }//if

            subsets.add(filePieces);
        }//while

        return subsets;
    }//makeSubsets

    private Path joinFiles(TreeSet<Path> splitFilesSet,
            TreeSet<Path> filesToDelete)
    {
        Path joinedPath = splitFilesSet.first();
        Path filename = joinedPath.getFileName();
        String joinedName = filename.toString().substring(0,
                filename.toString().length() - 6);

        Path outPath;
        Path destinationFolderToRename = null;

        if(destination == null)
        {
            outPath = joinedPath.resolveSibling(joinedName);
        }//if

        else
        {
            if(recreate.isSelected())
            {
                int index = searcher.getSelectedFile().toPath().
                        getNameCount() - 1;
                Path temp = joinedPath.resolveSibling(joinedName);
                temp = temp.subpath(index, temp.getNameCount());
                outPath = destination.resolve(temp);

                try
                {
                    Files.createDirectories(outPath.resolveSibling(""));
                }//try

                catch(IOException ex){}//catch

                destinationFolderToRename = findParent(outPath);
            }//if

            else
            {
                outPath = destination.resolve(joinedName);
            }//else
        }//else

        try
        {
            int filePieces = splitFilesSet.size();
            long joinedFilePosition = 0;
            long joinedFilesize = 0;

            for(Path piece : splitFilesSet)
            {
                joinedFilesize += Files.size(piece);
            }//for

            try(FileChannel out = new FileOutputStream(outPath.toFile()).
                    getChannel())
            {
                for(int i = 0 ; i < filePieces ; i++)
                {
                    Path filePiece = splitFilesSet.pollFirst();
                    long splitFilePosition = 0;
                    long bytesRemainingForPiece = Files.size(filePiece);

                    try(FileChannel in = new FileInputStream(filePiece.
                            toFile()).getChannel())
                    {
                        while(bytesRemainingForPiece != 0)
                        {
                            long transferred;

                            if(bytesRemainingForPiece < Integer.MAX_VALUE)
                            {
                                transferred = in.transferTo(splitFilePosition,
                                        bytesRemainingForPiece, out);
                            }//if

                            else
                            {
                                transferred = in.transferTo(splitFilePosition,
                                        Integer.MAX_VALUE, out);
                            }//else

                            splitFilePosition += transferred;
                            joinedFilePosition += transferred;
                            bytesRemainingForPiece -= transferred;
                            fileProgress.setValue((int) ((
                                    (double) joinedFilePosition /
                                    (double) joinedFilesize) * 100.0));
                        }//while
                    }//try

                    filesToDelete.add(filePiece);
                }//for
            }//try
        }//try

        catch(IOException ex)
        {
            quit("Error occurred while joining " + joinedName + ".");
        }//catch

        return destinationFolderToRename;
    }//joinFiles

    private Path splitFile(Path largeFile)
    {
        Path destinationFolderToRename = null;

        try
        {
            long largeFilePosition = 0;
            long largeFilesize = Files.size(largeFile);

            int chunksDone = 0;
            int totalChunks = (int) Math.ceil((double) largeFilesize /
                    (double) CHUNK_SIZE);
            int filePieces = (int) Math.ceil((double) largeFilesize /
                    (double) MAX_SIZE);

            try(FileChannel in = new FileInputStream(largeFile.toFile()).
                    getChannel())
            {
                for(int i = 0 ; i < filePieces ; i++)
                {
                    long bytesRemainingForPiece;

                    if(i == filePieces - 1)
                    {
                        bytesRemainingForPiece = largeFilesize -
                                largeFilePosition;
                    }//if

                    else
                    {
                        bytesRemainingForPiece = MAX_SIZE;
                    }//else

                    String splitName = largeFile.getFileName().toString() +
                            EXTENSION_PREFIX + String.format("%02d", i);
                    Path outPath;

                    if(destination == null)
                    {
                        outPath = largeFile.resolveSibling(splitName);
                    }//if

                    else
                    {
                        if(recreate.isSelected())
                        {
                            int index = searcher.getSelectedFile().toPath().
                                    getNameCount() - 1;
                            Path temp = largeFile.resolveSibling(splitName);
                            temp = temp.subpath(index, temp.getNameCount());
                            outPath = destination.resolve(temp);
                            Files.createDirectories(outPath.resolveSibling(""));

                            if(i == filePieces - 1)
                            {
                                destinationFolderToRename = findParent(outPath);
                            }//if
                        }//if

                        else
                        {
                            outPath = destination.resolve(splitName);
                        }//else
                    }//else

                    try(FileChannel out = new FileOutputStream(outPath.
                            toFile()).getChannel())
                    {
                        while(bytesRemainingForPiece != 0)
                        {
                            long transferred;

                            if(bytesRemainingForPiece < CHUNK_SIZE)
                            {
                                transferred = in.transferTo(largeFilePosition,
                                        bytesRemainingForPiece, out);
                            }//if

                            else
                            {
                                transferred = in.transferTo(largeFilePosition,
                                        CHUNK_SIZE, out);
                            }//else

                            largeFilePosition += transferred;
                            bytesRemainingForPiece -= transferred;
                            fileProgress.setValue((int) (((double) ++chunksDone
                                    / (double) totalChunks) * 100.0));
                        }//while
                    }//try
                }//for
            }//try
        }//try

        catch(IOException ex)
        {
            quit("Error occurred while splitting " + largeFile.getFileName().
                    toString() + ".");
        }//catch

        return destinationFolderToRename;
    }//splitFile

    private void deleteFiles(TreeSet<Path> filesToDelete)
    {
        for(Path deleteThis : filesToDelete)
        {
            try
            {
                Files.deleteIfExists(deleteThis);
            }//try

            catch(IOException ex)
            {
                quit("Error occurred while deleting " +
                        deleteThis.getFileName().toString() + ".");
            }//catch
        }//for
    }//deleteFiles

    private void renameFolders(TreeSet<Path> foldersToRename)
    {
        for(Path path : foldersToRename)
        {
            Path folder = path.getFileName();
            Path renamed = null;

            if(join.isSelected())
            {
                if(folder.getFileName().toString().startsWith("_"))
                {
                    renamed = path.resolveSibling(folder.getFileName().
                            toString().substring(1));
                }//if
            }//if

            else if(split.isSelected())
            {
                if(!folder.getFileName().toString().startsWith("_"))
                {
                    renamed = path.resolveSibling("_" + folder.getFileName().
                            toString());
                }//if
            }//else if

            try
            {
                 Files.move(path, renamed,
                         StandardCopyOption.REPLACE_EXISTING);
            }//try

            catch(IOException ex)
            {
                quit("Error while renaming " + path.getFileName() + ".");
            }//catch
        }//for
    }//renameFolders

    private void quit(String details)
    {
        JLabel message = new JLabel(details + " Exiting.");
        JOptionPane.showMessageDialog(jframe, message, TITLE,
                JOptionPane.PLAIN_MESSAGE);
        System.exit(0);
    }//quit

    private class Task extends SwingWorker<Void, Void>
    {
        ArrayList<TreeSet<Path>> treeList;

        public Task(ArrayList<TreeSet<Path>> treeList)
        {
            this.treeList = treeList;
        }//constructor

        @Override
        public Void doInBackground()
        {
            fileProgress.setString(null);
            totalProgress.setString(null);
            totalProgress.setValue(0);
            join.setEnabled(false);
            split.setEnabled(false);
            delete.setEnabled(false);
            rename.setEnabled(false);
            change.setEnabled(false);
            recreate.setEnabled(false);
            search.setEnabled(false);

            if(join.isSelected())
            {
                join(treeList);
            }//if

            else if(split.isSelected())
            {
               split(treeList);
            }//else if

            if(delete.isSelected())
            {
                deleteFiles(treeList.get(0));
            }//if

            if(rename.isSelected())
            {
                renameFolders(treeList.get(1));
            }//if

            return null;
        }//doInBackground

        @Override
        public void done()
        {
            filesFound.setText(" ");
            info.setText("Task(s) Completed Successfully");
            jframe.pack();
            fileProgress.setValue(0);
            join.setEnabled(true);
            split.setEnabled(true);
            delete.setEnabled(true);
            rename.setEnabled(true);
            change.setEnabled(true);
            recreate.setEnabled(true);
            search.setEnabled(true);
            searcher = null;
            destination = null;
        }//done
    }//Task
}//SimpleSplitPS3