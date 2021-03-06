package com.indeed.proctor.store;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.util.TimerTask;

/**
 * Timer task used to periodically run git fetch/reset in a git directory
 */
public class GitDirectoryRefresher extends TimerTask {
    private static final Logger LOGGER = Logger.getLogger(GitDirectoryRefresher.class);
    private static final TextProgressMonitor PROGRESS_MONITOR = new TextProgressMonitor(new LoggerPrintWriter(LOGGER, Level.DEBUG));
    private final File directory;
    private final GitProctorCore gitProctorCore;
    private final UsernamePasswordCredentialsProvider user;

    GitDirectoryRefresher(final File directory,
                          final GitProctorCore git,
                          final String username,
                          final String password) {
        this.directory = directory;
        this.gitProctorCore = git;
        this.user = new UsernamePasswordCredentialsProvider(username, password);
    }

    @Override
    public void run() {
        try {
            synchronized (directory) {
                /** git pull is preferable since it's more efficient **/
                final PullResult result = gitProctorCore.getGit().pull().setProgressMonitor(PROGRESS_MONITOR).setRebase(true).setCredentialsProvider(user).call();
                if (!result.isSuccessful()) {
                    /** if git pull failed, use git reset **/
                    gitProctorCore.undoLocalChanges();
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Error when refreshing git directory " + directory, e);
        }
    }

    public String getDirectoryPath() {
        return directory.getPath();
    }
}
