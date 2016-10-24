/*
 * Copyright (C) 2016 profiq Inc. http://www.profiq.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.profiq.techresearch.mule.gitlab;

import com.profiq.techresearch.mule.gitlab.config.ConnectorConfig;
import org.gitlab.api.Pagination;
import org.gitlab.api.models.*;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.display.FriendlyName;
import org.mule.api.annotations.display.Password;
import org.mule.api.annotations.param.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Gitlab connector for MuleSoft
 *
 * @author Filip Vavera
 */
@Connector(name="gitlab", friendlyName="Gitlab", minMuleVersion = "3.7")
public class GitlabConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabConnector.class);

    @Config
    private ConnectorConfig config;

    /**
     * Config getter
     *
     * @return config instance
     */
    public ConnectorConfig getConfig() {
        return this.config;
    }

    /**
     * Config setter
     *
     * @param config config instance
     */
    public void setConfig(final ConnectorConfig config) {
        this.config = config;
    }

    /**
     * Accept merge request
     *
     * @param projectId ID of the project
     * @param mergeRequestId ID of the Merge Request
     * @param mergeCommitMessage merge commit message (optional)
     * @return Gitlab merge request
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabMergeRequest acceptMergeRequest(@FriendlyName("Project ID") final Integer projectId,
                                                 @FriendlyName("Merge Request ID") final Integer mergeRequestId,
                                                 @Optional final String mergeCommitMessage) throws IOException {
        final GitlabProject project;
        final GitlabMergeRequest result;

        LOGGER.trace("Trying to load project {}...", projectId);
        try {
            project = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("Project {} was not loaded.", projectId);
            throw ex;
        }
        if (project == null) {
            LOGGER.error("Project {} was not loaded (return \"null\").", projectId);
            throw new IOException("Project was not loaded (return \"null\").");
        }
        LOGGER.debug("Project {} was loaded correctly.", projectId);

        LOGGER.trace("Trying to accept merge request {}...", mergeRequestId);
        try {
            result = this.config.getApiHandler().acceptMergeRequest(project, mergeRequestId, mergeCommitMessage);
        }
        catch (final IOException ex) {
            LOGGER.error("Accepting merge request {} failed.", mergeRequestId);
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Accepting merge request {} failed (return \"null\").", mergeRequestId);
            throw new IOException("Accepting merge request failed (return \"null\").");
        }
        LOGGER.debug("Merge request {} was accepted correctly.", mergeRequestId);

        return result;
    }

    /**
     * Add group member
     *
     * @param groupId ID of the group
     * @param userId ID of the user
     * @param accessLevel user access level (optional)
     * @return Gitlab group member
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabGroupMember addGroupMember(@FriendlyName("Group ID") final Integer groupId,
                                            @FriendlyName("User ID") final Integer userId,
                                            @Optional final GitlabAccessLevel accessLevel) throws IOException{
        final GitlabGroupMember result;

        LOGGER.trace("Trying to add user {} to the group {}...", userId, groupId);
        try {
            result = this.config.getApiHandler().addGroupMember(groupId, userId, accessLevel);
        }
        catch (final IOException ex) {
            LOGGER.error("Adding user {} to the group {} failed.", groupId, userId);
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Adding user {} to the group {} (return \"null\").", userId, groupId);
            throw new IOException("Adding group member failed (return \"null\").");
        }
        LOGGER.debug("User {} was correctly added to the group {}.", userId, groupId);

        return result;
    }

    /**
     * Add project member
     *
     * @param projectId ID of the project
     * @param userId ID of the user
     * @param accessLevel user access level (optional)
     * @return Gitlab project member
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabProjectMember addProjectMember(@FriendlyName("Project ID") final Integer projectId,
                                                @FriendlyName("User ID") final Integer userId,
                                                @Optional final GitlabAccessLevel accessLevel) throws IOException {
        final GitlabProjectMember result;

        LOGGER.trace("Trying to add user {} to the project {}...", userId, projectId);
        try {
            result = this.config.getApiHandler().addProjectMember(projectId, userId, accessLevel);
        }
        catch (final IOException ex) {
            LOGGER.error("User {} was not added to the project {}.", projectId, userId);
            throw ex;
        }
        if (result == null) {
            LOGGER.error("User {} was not added to the project {} (return \"null\").", userId, projectId);
            throw new IOException("User was not added to the project (return \"null\").");
        }
        LOGGER.debug("User {} was correctly added to the project {}.", userId, projectId);

        return result;
    }

    /**
     * Add project hook with default settings
     *
     * @param projectId ID of the project
     * @param url hook URL
     * @return Gitlab project hook
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabProjectHook addDefaultProjectHook(@FriendlyName("Project ID") final Integer projectId,
                                                   @FriendlyName("Hook URL") final String url) throws IOException {
        final GitlabProject project;
        final GitlabProjectHook result;

        LOGGER.trace("Trying to load project {}...", projectId);
        try {
            project = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("Project {} was not loaded.", projectId);
            throw ex;
        }
        if (project == null) {
            LOGGER.error("Project {} was not loaded (return \"null\").", projectId);
            throw new IOException("Project was not loaded (return \"null\").");
        }
        LOGGER.debug("Project {} was loaded correctly.", projectId);

        LOGGER.trace("Trying to add default project hook...");
        try {
            result = this.config.getApiHandler().addProjectHook(project, url);
        }
        catch (final IOException ex) {
            LOGGER.error("Adding project hook failed.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Adding project hook failed (return \"null\").");
            throw new IOException("Adding project hook failed (return \"null\").");
        }
        LOGGER.debug("Project hook added correctly.");

        return result;
    }

    /**
     * Add project hook with all options
     *
     * @param projectId ID of the project
     * @param url hook URL
     * @param pushEvents push events enabled
     * @param issuesEvents issue events enabled
     * @param mergeRequestEvents merge request events enabled
     * @param sslVerification SSL verification enabled
     * @return Gitlab project hook
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabProjectHook addProjectHook(
            @FriendlyName("Project ID") final Integer projectId, @FriendlyName("Hook URL") final String url,
            @FriendlyName("Enable push events") final Boolean pushEvents,
            @FriendlyName("Enable issue events") final Boolean issuesEvents,
            @FriendlyName("Enable merge request events") final Boolean mergeRequestEvents,
            @FriendlyName("Enable SSL verification") final Boolean sslVerification) throws IOException {
        final GitlabProjectHook result;

        LOGGER.trace("Trying to add project hook...");
        try {
            result = this.config.getApiHandler().addProjectHook(projectId, url, pushEvents, issuesEvents,
                    mergeRequestEvents, sslVerification);
        }
        catch (final IOException ex) {
            LOGGER.error("Adding project hook failed.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Adding project hook has failed (return \"null\").");
            throw new IOException("Adding project has hook failed (return \"null\").");
        }
        LOGGER.debug("Project hook added correctly.");

        return result;
    }

    /**
     * Add system hook
     *
     * @param url hook URL
     * @return Gitlab system hook
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabSystemHook addSystemHook(@FriendlyName("Hook URL") final String url) throws IOException {
        final GitlabSystemHook result;

        LOGGER.trace("Trying to add system hook...");
        try {
            result = this.config.getApiHandler().addSystemHook(url);
        }
        catch (final IOException ex) {
            LOGGER.error("Adding system hook failed.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Adding system hook failed (return \"null\").");
            throw new IOException("Adding system hook failed (return \"null\").");
        }
        LOGGER.debug("System hook added correctly.");

        return result;
    }

    /**
     * Block user
     *
     * @param userId ID of the user
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public void blockUser(@FriendlyName("User ID") final Integer userId) throws IOException {
        LOGGER.trace("Trying to block user {}...", userId);
        try {
            this.config.getApiHandler().blockUser(userId);
        }
        catch (final IOException ex) {
            LOGGER.error("Blocking user {} failed.", userId);
            throw ex;
        }
        LOGGER.debug("Blocking user was successful.");
    }

    /**
     * Create branch
     *
     * @param projectId ID of the project
     * @param branchName name of the new branch
     * @param ref branch reference (commit hash)
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public void createBranch(@FriendlyName("Project ID") final Integer projectId, final String branchName,
                             @FriendlyName("Branch reference") final String ref) throws IOException {
        LOGGER.trace("Trying to create branch in project {}...", projectId);
        try {
            this.config.getApiHandler().createBranch(projectId, branchName, ref);
        }
        catch (final IOException ex) {
            LOGGER.error("Creating branch has failed.");
            throw ex;
        }
        LOGGER.debug("Branch was successfully created.");
    }

    /**
     * Create commit comment
     *
     * @param projectId ID of the project
     * @param commitHash hash of the commit
     * @param note comment note (optional)
     * @param path (optional)
     * @param line (optional)
     * @param lineType (optional)
     * @return Gitlab commit comment
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public CommitComment createCommitComment(@FriendlyName("Project ID") final Integer projectId,
                                             final String commitHash, @Optional final String note,
                                             @Optional final String path, @Optional final String line,
                                             @Optional final String lineType) throws IOException {
        final CommitComment result;

        LOGGER.trace("Trying to create commit comment...");
        try {
            result = this.config.getApiHandler().createCommitComment(projectId, commitHash, note, path, line, lineType);
        }
        catch (final IOException ex) {
            LOGGER.error("Creating commit comment failed.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Creating commit comment failed (return \"null\")");
            throw new IOException("Creating commit comment failed (return \"null\")");
        }
        LOGGER.debug("Commit comment created correctly.");

        return result;
    }

    /**
     * Create commit status
     *
     * @param projectId ID of the project
     * @param commitHash hash of the commit
     * @param state commit state
     * @param ref reference
     * @param commitName name of the commit
     * @param targetUrl URL of the target
     * @param description description
     * @return Gitlab commit status
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabCommitStatus createCommitStatus(
            @FriendlyName("Project ID") final Integer projectId, final String commitHash, final String state,
            @FriendlyName("Reference") final String ref, final String commitName,
            @FriendlyName("Target URL") final String targetUrl, final String description) throws IOException {
        final GitlabProject project;
        final GitlabCommitStatus result;

        LOGGER.trace("Trying to load project {}...", projectId);
        try {
            project = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("Loading project {} failed.", projectId);
            throw ex;
        }
        if (project == null) {
            LOGGER.error("Loading project {} failed (return \"null\").", projectId);
            throw new IOException("Loading project failed (return \"null\").");
        }
        LOGGER.debug("Project {} loaded correctly.", projectId);

        LOGGER.trace("Trying to create commit status...");
        try {
            result = this.config.getApiHandler().createCommitStatus(project, commitHash, state, ref, commitName,
                    targetUrl, description);
        }
        catch (final IOException ex) {
            LOGGER.error("Creating commit status failed.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Creating commit status failed (return \"null\").");
            throw new IOException("Creating commit status failed (return \"null\").");
        }
        LOGGER.debug("Commit status created correctly.");

        return result;
    }

    /**
     * Create deploy key
     *
     * @param projectId ID of the project
     * @param title deploy key title
     * @param key deploy key
     * @return Gitlab SSH key
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabSSHKey createDeployKey(@FriendlyName("Project ID") final Integer projectId, final String title,
                                        final String key) throws IOException {
        final GitlabSSHKey result;

        LOGGER.trace("Trying to create deploy key...");
        try {
            result = this.config.getApiHandler().createDeployKey(projectId, title, key);
        }
        catch (final IOException ex) {
            LOGGER.error("Creating deploy key failed.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Creating deploy key failed (return \"null\").");
            throw new IOException("Creating deploy key failed (return \"null\").");
        }
        LOGGER.debug("Deploy key created correctly.");

        return result;
    }

    /**
     * Create group
     *
     * @param groupName name of the new group
     * @param path group path (optional)
     * @param ldapCn (optional)
     * @param ldapAccess LDAP access (optional)
     * @param sudoUserId  ID of the user with sudo privileges to the group (optional)
     * @return Gitlab group
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabGroup createGroup(final String groupName, @Optional final String path,
                                   @Optional @FriendlyName("LDAP Common Name") final String ldapCn,
                                   @Optional final GitlabAccessLevel ldapAccess,
                                   @Optional @FriendlyName("Sudo User ID") final Integer sudoUserId)
            throws IOException {
        GitlabUser sudoUser = null;
        final GitlabGroup result;

        if (sudoUserId != null) {
            LOGGER.trace("Trying to load sudo user {}...", sudoUserId);
            try {
                sudoUser = this.config.getApiHandler().getUser(sudoUserId);
            }
            catch (final IOException ex) {
                LOGGER.error("Sudo user {} was not loaded.", sudoUserId);
                throw ex;
            }
            if (sudoUser == null) {
                LOGGER.error("Sudo user {} was not loaded (return \"null\").", sudoUserId);
                throw new IOException("Sudo user was not loaded (return \"null\").");
            }
            LOGGER.debug("Sudo user {} loaded correctly.", sudoUserId);
        }

        LOGGER.trace("Trying to create group...");
        try {
            result = this.config.getApiHandler().createGroup(groupName, path, ldapCn, ldapAccess, sudoUser);
        }
        catch (final IOException ex) {
            LOGGER.error("Group was not created.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Group was not created.");
            throw new IOException("Group was not created.");
        }
        LOGGER.debug("Group created correctly.");

        return result;
    }

    /**
     * Create group via sudo
     *
     * @param groupName new group name
     * @param path group path
     * @param sudoUserId sudo user ID
     * @return Gitlab group
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabGroup createGroupViaSudo(final String groupName, final String path,
                                          @FriendlyName("Sudo User ID") final Integer sudoUserId) throws IOException {
        final GitlabUser sudoUser;
        final GitlabGroup result;

        LOGGER.trace("Trying to load sudo user {}...", sudoUserId);
        try {
            sudoUser = this.config.getApiHandler().getUser(sudoUserId);
        }
        catch (final IOException ex) {
            LOGGER.error("Sudo user {} was not loaded.", sudoUserId);
            throw ex;
        }
        if (sudoUser == null) {
            LOGGER.error("Sudo user {} was not loaded (return \"null\").", sudoUserId);
            throw new IOException("Sudo user was not loaded (return \"null\").");
        }
        LOGGER.debug("Sudo user {} loaded correctly.", sudoUserId);

        LOGGER.trace("Trying to create group...");
        try {
            result = this.config.getApiHandler().createGroupViaSudo(groupName, path, sudoUser);
        }
        catch (final IOException ex) {
            LOGGER.error("Group was not created.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Group was not created.");
            throw new IOException("Group was not created.");
        }
        LOGGER.debug("Group created correctly.");

        return result;
    }

    /**
     * Create issue
     *
     * @param projectId ID of the project
     * @param title issue title
     * @param description issue description
     * @param assigneeId ID of the assigned user (optional)
     * @param milestoneId ID of the milestone (optional)
     * @param labels issue labels (optional)
     * @return Gitlab issue
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabIssue createIssue(@FriendlyName("Project ID") final Integer projectId, final String title,
                                   final String description, @Optional @FriendlyName("Assignee ID") Integer assigneeId,
                                   @Optional @FriendlyName("Milestone ID") final Integer milestoneId,
                                   @Optional final String labels) throws IOException {
        if (assigneeId == null) {
            assigneeId = 0;
        }

        final GitlabIssue result;

        LOGGER.trace("Trying to crate issue...");
        try {
            result = this.config.getApiHandler().createIssue(projectId, assigneeId, milestoneId, labels, description, title);
        }
        catch (final IOException ex) {
            LOGGER.error("Creating issue failed.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Creating issue failed (return \"null\").");
            throw new IOException("Creating issue failed (return \"null\").");
        }
        LOGGER.debug("Issue created successfully.");

        return result;
    }

    /**
     * Create label
     *
     * @param projectId ID of the project
     * @param labelName name of the label
     * @param color label color
     * @return Gitlab label
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabLabel createLabel(final Integer projectId, final String labelName, final String color)
            throws IOException {

        final GitlabLabel result;

        LOGGER.trace("Trying to create label...");
        try {
            result = this.config.getApiHandler().createLabel(projectId, labelName, color);
        }
        catch (final IOException ex) {
            LOGGER.error("Label was not created correctly.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Label was not created correctly (return \"null\").");
            throw new IOException("Label was not created correctly (return \"null\").");
        }
        LOGGER.debug("Label was created correctly.");

        return result;
    }

//    @Processor
//    public GitlabMergeRequest createMergeRequest(final Integer projectId, final String sourceBranch,
//                                                 final String targetBranch, final Integer assigneeId,
//                                                 final String title) throws IOException {
//        return this.config.getApiHandler().createMergeRequest(projectId, sourceBranch, targetBranch, assigneeId,
//                                                              title);
//    }

    /**
     * Create milestone
     *
     * @param projectId ID of the project
     * @param title milestone's title
     * @param description milestone's description
     * @param dueDate due date
     * @return Gitlab milestone
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabMilestone createMilestone(final Integer projectId, final String title, final String description,
                                           final org.mule.api.el.datetime.Date dueDate) throws IOException {
        final LocalDate date = LocalDate.of(dueDate.getYear(), dueDate.getMonth() - 1, dueDate.getDayOfMonth());
        return this.config.getApiHandler().createMilestone(projectId, title, description,
                Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    /**
     * Create issue note
     *
     * @param projectId ID of the project
     * @param issueId ID of the issue
     * @param message note message
     * @return Gitlab note
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabNote createIssueNote(final Integer projectId, final Integer issueId, final String message)
            throws IOException {
        return this.config.getApiHandler().createNote(projectId, issueId, message);
    }

    /**
     * Create merge request note
     *
     * @param projectId ID of the project
     * @param mergeRequestId ID of the merge request
     * @param message note message
     * @return Gitlab note
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabNote createMergeRequestNote(final Integer projectId, final Integer mergeRequestId, final String message)
            throws IOException {
        final GitlabMergeRequest mergeRequest = this.config.getApiHandler().getMergeRequestByIid(projectId,
                mergeRequestId);
        return this.config.getApiHandler().createNote(mergeRequest, message);
    }

    /**
     * Create project
     *
     * @param projectName name of the project
     * @param namespaceId ID of the namespace (optional)
     * @param description project's description (optional)
     * @param issuesEnabled are issues in this project enabled?
     * @param wallEnabled is wall for this project enabled?
     * @param mergeRequestsEnabled are merge requests for this project enabled?
     * @param wikiEnabled is wiki for this project enabled?
     * @param snippetsEnabled are snippets for this project enabled?
     * @param publik is project public?
     * @param visibilityLevel visibility level of the project (optional)
     * @param importUrl import project from this URL (optional)
     * @return Gitlab project
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabProject createProject(
            final String projectName, @Optional final Integer namespaceId, @Optional final String description,
            @Optional final Boolean issuesEnabled, @Optional final Boolean wallEnabled,
            @Optional final Boolean mergeRequestsEnabled, @Optional final Boolean wikiEnabled,
            @Optional final Boolean snippetsEnabled, @Optional @FriendlyName("Public") final Boolean publik,
            @Optional final Integer visibilityLevel, @Optional @FriendlyName("Import URL") final String importUrl)
            throws IOException {
        return this.config.getApiHandler().createProject(projectName, namespaceId, description, issuesEnabled,
                wallEnabled, mergeRequestsEnabled, wikiEnabled, snippetsEnabled, publik, visibilityLevel, importUrl);
    }

    /**
     * Create SSH key
     *
     * @param userId ID of the user
     * @param title SSH key title
     * @param key SSH key
     * @return Gitlab SSH key
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor(friendlyName = "Create SSH key")
    public GitlabSSHKey createSSHKey(@FriendlyName("User ID") final Integer userId, final String title,
                                     final String key) throws IOException {
        final GitlabSSHKey result;

        LOGGER.trace("Trying to create SSH key...");
        try {
            result = this.config.getApiHandler().createSSHKey(userId, title, key);
        }
        catch (final IOException ex) {
            LOGGER.error("SSH key was not created.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("SSH key was not created (return \"null\").");
            throw new IOException("SSH key was not created (return \"null\").");
        }
        LOGGER.debug("SSH key was created correctly.");

        return result;
    }

    /**
     * Create user
     *
     * @param email user's email
     * @param password users's password
     * @param username user's username
     * @param fullName user's full name
     * @param skypeId user's Skype ID
     * @param linkedIn user's LinkedIn
     * @param twitter user's twitter
     * @param websiteUrl user's URL
     * @param projectsLimit user's limit for the projects
     * @param externUid user's extern UID
     * @param externProviderName user's extern provider name
     * @param bio user's bio
     * @param isAdmin is user admin?
     * @param canCreateGroup is user allowed to create groups?
     * @param skipConfirmation should be confirmation skipped?
     * @return Gitlab user
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabUser createUser(@FriendlyName("E-mail") final String email, @Optional @Password final String password,
                                 @Optional final String username, @Optional final String fullName,
                                 @FriendlyName("Skype ID") @Optional final String skypeId,
                                 @Optional final String linkedIn, @Optional final String twitter,
                                 @FriendlyName("Website URL") @Optional final String websiteUrl,
                                 @Optional final Integer projectsLimit,
                                 @FriendlyName("Extern UID") @Optional final String externUid,
                                 @Optional final String externProviderName, @Optional final String bio,
                                 @Optional final Boolean isAdmin, @Optional final Boolean canCreateGroup,
                                 @Optional final Boolean skipConfirmation) throws IOException {
        final GitlabUser result;

        LOGGER.trace("Trying to create user...");
        try {
            result = this.config.getApiHandler().createUser(email, password, username, fullName, skypeId, linkedIn,
                    twitter, websiteUrl, projectsLimit, externUid, externProviderName, bio, isAdmin, canCreateGroup,
                    skipConfirmation);
        }
        catch (final IOException ex) {
            LOGGER.error("User was not created correctly.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("User was not created correctly (return \"null\").");
            throw new IOException("User was not created correctly (return \"null\").");
        }
        LOGGER.debug("User was created.");

        return result;
    }

    /**
     * Create user project
     *
     * @param userId ID of the user
     * @param projectName name of the project
     * @param description project's description (optional)
     * @param defaultBranch project's default branch (optional)
     * @param issuesEnabled are issues enabled? (optional)
     * @param wallEnabled is wall enabled? (optional)
     * @param mergeRequestsEnabled are merge requests enabled? (optional)
     * @param wikiEnabled is wiki enabled? (optional)
     * @param snippetsEnabled are snippets enabled? (optional)
     * @param publik is project public? (optional)
     * @param visibilityLevel project's visibility level (optional)
     * @param importUrl import URL for the project (if you want to import project) (optional)
     * @return Gitlab project
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabProject createUserProject(@FriendlyName("User ID") final Integer userId, final String projectName,
                                           @Optional final String description, @Optional final String defaultBranch,
                                           @Optional final Boolean issuesEnabled, @Optional final Boolean wallEnabled,
                                           @Optional final Boolean mergeRequestsEnabled,
                                           @Optional final Boolean wikiEnabled, @Optional final Boolean snippetsEnabled,
                                           @FriendlyName("Public") @Optional final Boolean publik,
                                           @Optional final Integer visibilityLevel, @Optional final String importUrl)
            throws IOException {
        final GitlabProject result;

        LOGGER.trace("Trying to create user project...");
        try {
            result = this.config.getApiHandler().createUserProject(userId, projectName, description, defaultBranch,
                    issuesEnabled, wallEnabled, mergeRequestsEnabled, wikiEnabled, snippetsEnabled, publik,
                    visibilityLevel, importUrl);
        }
        catch (final IOException ex) {
            LOGGER.error("Creating user project failed.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Creating user project failed (return \"null\").");
            throw new IOException("Creating user project failed (return \"null\").");
        }
        LOGGER.debug("Project was created successfully.");

        return result;
    }

    /**
     * Delete branch
     *
     * @param projectId ID of the project
     * @param branchName branch name
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public void deleteBranch(@FriendlyName("Project ID") final Integer projectId, final String branchName)
            throws IOException {
        LOGGER.trace("Trying to delete branch...");
        try {
            this.config.getApiHandler().deleteBranch(projectId, branchName);
        }
        catch (final IOException ex) {
            LOGGER.error("Branch was not deleted.");
            throw ex;
        }
        LOGGER.debug("Branch was deleted.");
    }

    /**
     * Delete deploy key
     *
     * @param projectId ID of the project
     * @param keyId ID of the key
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public void deleteDeployKey(@FriendlyName("Project ID") final Integer projectId,
                                @FriendlyName("Key ID") final Integer keyId) throws IOException {
        LOGGER.trace("Trying to delete deploy kay...");
        try {
            this.config.getApiHandler().deleteDeployKey(projectId, keyId);
        }
        catch (final IOException ex) {
            LOGGER.error("Deploy key was not deleted.");
            throw ex;
        }
        LOGGER.debug("Deploy key was deleted.");
    }

    /**
     * Delete group
     *
     * @param groupId ID of the group
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public void deleteGroup(@FriendlyName("Group ID") final Integer groupId) throws IOException {
        LOGGER.trace("Trying to delete group...");
        try {
            this.config.getApiHandler().deleteGroup(groupId);
        }
        catch (final IOException ex) {
            LOGGER.error("Deleting group failed.");
            throw ex;
        }
        LOGGER.debug("Group was correctly deleted.");
    }

    /**
     * Delete group member
     *
     * @param groupId ID of the group
     * @param userId ID of the group
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public void deleteGroupMember(@FriendlyName("Group ID") final Integer groupId,
                                  @FriendlyName("User ID") final Integer userId) throws IOException {
        LOGGER.trace("Trying to delete group member");
        try {
            this.config.getApiHandler().deleteGroupMember(groupId, userId);
        }
        catch (final IOException ex) {
            LOGGER.error("Deleting group member failed.");
            throw ex;
        }
        LOGGER.debug("Group member was deleted.");
    }

    /**
     * Delete label
     *
     * @param projectId ID of the project
     * @param labelName name of the label
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public void deleteLabel(@FriendlyName("Project ID") final Integer projectId, final String labelName)
            throws IOException {
        LOGGER.trace("Trying to delete label.");
        try {
            this.config.getApiHandler().deleteLabel(projectId, labelName);
        }
        catch (final IOException ex) {
            LOGGER.error("Deleting label failed.");
            throw ex;
        }
        LOGGER.debug("Label was correctly deleted.");
    }

    /**
     * Delete project
     *
     * @param projectId ID of the project
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public void deleteProject(final Integer projectId) throws IOException {
        LOGGER.trace("Trying to delete project...");
        try {
            this.config.getApiHandler().deleteProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("Deleting project failed.");
            throw ex;
        }
        LOGGER.debug("Project was correctly deleted.");
    }

    /**
     * Delete project hook
     *
     * @param projectId ID of the project
     * @param hookId ID of the hook
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public void deleteProjectHook(@FriendlyName("Project ID") final Integer projectId,
                                  @FriendlyName("Hook ID") final String hookId) throws IOException {
        final GitlabProject project;

        LOGGER.trace("Trying to load project {}...", projectId);
        try {
            project = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("Loading project failed.");
            throw ex;
        }
        if (project == null) {
            LOGGER.error("Loading project failed (return \"null\").");
            throw new IOException("Loading project failed (return \"null\").");
        }
        LOGGER.debug("Project was correctly loaded.");

        LOGGER.trace("Trying to delete project hook...");
        try {
            this.config.getApiHandler().deleteProjectHook(project, hookId);
        }
        catch (final IOException ex) {
            LOGGER.error("Deleting project hook failed.");
            throw ex;
        }
        LOGGER.debug("Project hook was correctly deleted.");
    }

    /**
     * Delete project member
     *
     * @param projectId ID of the project
     * @param userId ID of the user
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public void deleteProjectMember(@FriendlyName("Project ID") final Integer projectId,
                                    @FriendlyName("User ID") final Integer userId) throws IOException {
        LOGGER.trace("Trying to delete project member...");
        try {
            this.config.getApiHandler().deleteProjectMember(projectId, userId);
        }
        catch (final IOException ex) {
            LOGGER.error("Deleting project member failed.");
            throw ex;
        }
        LOGGER.debug("Project member was deleted.");
    }

    /**
     * Delete SSH key
     *
     * @param userId ID of the user
     * @param keyId ID of the key
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor(friendlyName = "Delete SSH key")
    public void deleteSSHKey(@FriendlyName("User ID") final Integer userId, @FriendlyName("Key ID") final Integer keyId)
            throws IOException {
        LOGGER.trace("Trying to delete SSH key...");
        try {
            this.config.getApiHandler().deleteSSHKey(userId, keyId);
        }
        catch (final IOException ex) {
            LOGGER.error("Deleting SSH key failed.");
            throw ex;
        }
        LOGGER.debug("SSH key was correctly deleted.");
    }

    /**
     * Delete system hook
     *
     * @param hookId ID of the hook
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public void deleteSystemHook(@FriendlyName("Hook ID") final Integer hookId) throws IOException {
        LOGGER.trace("Trying to delete system hook...");
        try {
            this.config.getApiHandler().deleteSystemHook(hookId);
        }
        catch (final IOException ex) {
            LOGGER.error("Deleting system hook failed.");
            throw ex;
        }
        LOGGER.debug("System hook was correctly deleted.");
    }

//    @Processor
//    public void deleteTag(final Integer projectId, final String tagName) throws IOException {
//        this.config.getApiHandler().deleteTag(projectId, tagName);
//    }

    /**
     * Delete user
     *
     * @param userId ID of the user
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public void deleteUser(@FriendlyName("User ID") final Integer userId) throws IOException {
        LOGGER.trace("Trying to delete user...");
        try {
            this.config.getApiHandler().deleteUser(userId);
        }
        catch (final IOException ex) {
            LOGGER.error("Deleting user failed.");
            throw ex;
        }
        LOGGER.debug("User was correctly deleted.");
    }

    /**
     * Edit issue
     *
     * @param projectId ID of the project
     * @param issueId ID of the issue
     * @param assigneeId ID of the assigned user (optional)
     * @param milestoneId ID of the milestone (optional)
     * @param labels issue's labels (optional)
     * @param description issue's description (optional)
     * @param title issue's title (optional)
     * @param action Gitlab issue action (optional)
     * @return Gitlab issue
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabIssue editIssue(@FriendlyName("Project ID") final Integer projectId,
                                 @FriendlyName("Issue ID") final Integer issueId,
                                 @FriendlyName("Assignee ID") @Optional Integer assigneeId,
                                 @FriendlyName("Milestone ID") @Optional final Integer milestoneId,
                                 @Optional final String labels, @Optional final String description,
                                 @Optional final String title, @Optional final GitlabIssue.Action action)
            throws IOException {
        final GitlabIssue result;
        if (assigneeId == null) {
            assigneeId = 0;
        }

        LOGGER.trace("Trying to edit issue...");
        try {
            result = this.config.getApiHandler().editIssue(projectId, issueId, assigneeId, milestoneId, labels,
                    description, title, action);
        }
        catch (final IOException ex) {
            LOGGER.error("Editing issue failed.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Editing issue failed (return \"null\").");
            throw new IOException("Editing issue failed (return \"null\").");
        }
        LOGGER.debug("Issue was successfully edited.");

        return result;
    }

    /**
     * Edit project hook
     *
     * @param projectId ID of the project
     * @param hookId ID of the hook
     * @param url URL
     * @return Gitlab project hook
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabProjectHook editProjectHook(@FriendlyName("Project ID") final Integer projectId,
                                             @FriendlyName("Hook ID") final String hookId,
                                             @FriendlyName("URL") final String url) throws IOException {
        final GitlabProject project;
        final GitlabProjectHook result;

        LOGGER.trace("trying to load project {}...", projectId);
        try {
            project = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("Loading project {} failed.", projectId);
            throw ex;
        }
        if (project == null) {
            LOGGER.error("Loading project {} failed (return \"null\").", projectId);
            throw new IOException("Loading project failed (return \"null\").");
        }
        LOGGER.debug("Project {} was correctly loaded.", projectId);

        LOGGER.trace("Trying to edit project hook {}...", hookId);
        try {
            result = this.config.getApiHandler().editProjectHook(project, hookId, url);
        }
        catch (final IOException ex) {
            LOGGER.error("Editing project hook {} failed.", hookId);
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Editing project hook {} failed (return \"null\").", hookId);
            throw new IOException("Editing project hook failed (return \"null\").");
        }
        LOGGER.debug("Project hook was correctly edited.");

        return result;
    }

    /**
     * Find user
     *
     * @param emailOrUsername user's e-mail or username
     * @return list of Gitlab users
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabUser> findUsers(@FriendlyName("E-mail or Username") final String emailOrUsername)
            throws IOException {
        final List<GitlabUser> result;

        LOGGER.trace("Trying to find user...");
        try {
            result = this.config.getApiHandler().findUsers(emailOrUsername);
        }
        catch (final IOException ex) {
            LOGGER.error("Looking for user failed.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Looking for user failed (return \"null\").");
            throw new IOException("Looking for user failed (return \"null\").");
        }
        LOGGER.debug("Looking for user done correctly.");

        return result;
    }

    /**
     * Get all commits
     *
     * @param projectId ID of the project
     * @param pagination pagination
     * @param branchOrTag branch or tag
     * @return list of Gitlab commits
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabCommit> getAllCommits(@FriendlyName("Project ID") final Integer projectId,
                                            @Optional final Pagination pagination, @Optional final String branchOrTag)
            throws IOException {
        final List<GitlabCommit> result;

        LOGGER.trace("Trying to get commits...");
        try {
            result = this.config.getApiHandler().getAllCommits(projectId, pagination, branchOrTag);
        }
        catch (final IOException ex) {
            LOGGER.error("Loading commits failed.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Loading commits failed (return \"null\").");
            throw new IOException("Loading commits failed (return \"null\").");
        }
        LOGGER.debug("Commit correctly loaded.");

        return result;
    }

    /**
     * Get all merge requests
     *
     * @param projectId ID of the project
     * @return list of Gitlab Merge requests
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabMergeRequest> getAllMergeRequests(@FriendlyName("Project ID") final Integer projectId)
            throws IOException {
        final GitlabProject project;
        final List<GitlabMergeRequest> result;

        LOGGER.trace("Trying to load project {}...", projectId);
        try {
            project = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("Loading project failed.");
            throw ex;
        }
        if (project == null) {
            LOGGER.error("Loading project failed (return \"null\").");
            throw new IOException("Loading project failed (return \"null\").");
        }
        LOGGER.debug("Project {} loaded correctly.", projectId);

        LOGGER.trace("Trying to load merge requests...");
        try {
            result = this.config.getApiHandler().getAllMergeRequests(project);
        }
        catch (final IOException ex) {
            LOGGER.error("Loading merge requests failed.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Loading merge requests failed (return \"null\").");
            throw new IOException("Loading merge requests failed (return \"null\").");
        }
        LOGGER.debug("Merge requests loaded correctly.");

        return result;
    }

    /**
     * Get all notes
     *
     * @param projectId ID of the project
     * @param mergeRequestId ID of the merge request
     * @return List of Gitlab notes
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabNote> getAllNotes(@FriendlyName("Project ID") final Integer projectId,
                                        @FriendlyName("Merge request ID") final Integer mergeRequestId)
            throws IOException {
        final GitlabMergeRequest mergeRequest;
        final List<GitlabNote> result;

        LOGGER.trace("Trying to load merge request {} from the project {}...", mergeRequestId, projectId);
        try {
            mergeRequest = this.config.getApiHandler().getMergeRequestByIid(projectId, mergeRequestId);
        }
        catch (final IOException ex) {
            LOGGER.error("Loading merge request failed.");
            throw ex;
        }
        if (mergeRequest == null) {
            LOGGER.error("Loading merge request failed (return \"null\").");
            throw new IOException("Loading merge request failed (return \"null\").");
        }
        LOGGER.debug("Merge request {} from project {} loaded correctly.", mergeRequestId, projectId);

        LOGGER.trace("Trying to load notes...");
        try {
            result = this.config.getApiHandler().getAllNotes(mergeRequest);
        }
        catch (final IOException ex) {
            LOGGER.error("Loading notes failed.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Loading notes failed (return \"null\").");
            throw new IOException("Loading notes failed (return \"null\").");
        }
        LOGGER.debug("Notes loaded correctly.");

        return result;
    }

    /**
     * Get all projects
     *
     * @return List of Gitlab projects
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabProject> getAllProjects() throws IOException {
        final List<GitlabProject> result;

        LOGGER.trace("Trying to load projects...");
        try {
            result = this.config.getApiHandler().getAllProjects();
        }
        catch (final IOException ex) {
            LOGGER.error("Loading projects failed.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Loading projects failed (return \"null\").");
            throw new IOException("Loading projects failed (return \"null\").");
        }
        LOGGER.debug("Projects loaded correctly.");

        return result;
    }

    /**
     * Get branch
     *
     * @param projectId ID of the project
     * @param branchName name of the branch
     * @return Gitlab branch
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabBranch getBranch(@FriendlyName("Project ID") final Integer projectId, final String branchName)
            throws IOException {
        final GitlabProject project;
        final GitlabBranch result;

        LOGGER.trace("Trying to load project {}...", projectId);
        try {
            project = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("Loading project failed.");
            throw ex;
        }
        if (project == null) {
            LOGGER.error("Loading project failed (return \"null\").");
            throw new IOException("Loading project failed (return \"null\").");
        }
        LOGGER.debug("Project {} loaded correctly.", projectId);

        LOGGER.trace("Trying to load branch \"{}\"...", branchName);
        try {
            result = this.config.getApiHandler().getBranch(project, branchName);
        }
        catch (final IOException ex) {
            LOGGER.error("Loading branch failed.");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("Loading branch failed (return \"null\").");
            throw new IOException("Loading branch failed (return \"null\").");
        }
        LOGGER.debug("Branch loaded correctly.");

        return result;
    }

    /**
     * Get branches
     *
     * @param projectId ID of the project
     * @return list of Gitlab branches
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabBranch> getBranches(@FriendlyName("Project ID") final Integer projectId) throws IOException {
        final List<GitlabBranch> result;

        LOGGER.trace("Trying to get branches...");
        try {
            result = this.config.getApiHandler().getBranches(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

//    @Processor
//    public byte[] getBuildArtifact(final Integer projectId, final Integer buildId) throws IOException {
//        return this.config.getApiHandler().getBuildArtifact(projectId, buildId);
//    }

    /**
     * Get commit
     *
     * @param projectId ID of the project
     * @param commitHash hash of the commit
     * @return Gitlab commit
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabCommit getCommit(@FriendlyName("Project ID") final Integer projectId, final String commitHash)
            throws IOException {
        final GitlabCommit result;

        LOGGER.trace("Trying to get commits...");
        try {
            result = this.config.getApiHandler().getCommit(projectId, commitHash);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get commit builds
     *
     * @param projectId ID of the project
     * @param commitHash hash of the commit
     * @return list of Gitlab builds
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabBuild> getCommitBuilds(@FriendlyName("Project ID") final Integer projectId,
                                             final String commitHash) throws IOException {
        final List<GitlabBuild> result;

        LOGGER.trace("Trying to get commit builds...");
        try {
            result = this.config.getApiHandler().getCommitBuilds(projectId, commitHash);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get commit comments
     *
     * @param projectId ID of the project
     * @param commitHash hash of the commit
     * @return list of Gitlab commit comments
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<CommitComment> getCommitComments(@FriendlyName("Project ID") final Integer projectId,
                                                 final String commitHash) throws IOException {
        final List<CommitComment> result;

        LOGGER.trace("Trying to get commit comments...");
        try {
            result = this.config.getApiHandler().getCommitComments(projectId, commitHash);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get commit diffs
     *
     * @param projectId ID of the project
     * @param commitHash hash of the commit
     * @param pagination pagination
     * @return list of Gitlab commit diff
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabCommitDiff> getCommitDiffs(@FriendlyName("Project ID") final Integer projectId,
                                                 final String commitHash, @Optional final Pagination pagination)
            throws IOException {
        final List<GitlabCommitDiff> result;

        LOGGER.trace("Trying to get commit diffs...");
        try {
            result = this.config.getApiHandler().getCommitDiffs(projectId, commitHash, pagination);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get merge request commits
     *
     * @param projectId ID of the project
     * @param mergeRequestId ID of the merge request
     * @param pagination pagination
     * @return list of Gitlab commits
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabCommit> getMergeRequestCommits(@FriendlyName("Project ID") final Integer projectId,
                                                     @FriendlyName("Merge request ID") final Integer mergeRequestId,
                                                     @Optional final Pagination pagination) throws IOException {
        final GitlabMergeRequest mergeRequest;
        final List<GitlabCommit> result;

        LOGGER.trace("Trying to get merge request...");
        try {
            mergeRequest = this.config.getApiHandler().getMergeRequestByIid(projectId, mergeRequestId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (mergeRequest == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        LOGGER.trace("Trying to get merge request commits...");
        try {
            result = this.config.getApiHandler().getCommits(mergeRequest, pagination);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get project commits
     *
     * @param projectId ID of the project
     * @param branchOrTag name of the branch or tag
     * @param pagination pagination
     * @return list of Gitlab commits
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabCommit> getProjectCommits(@FriendlyName("Project ID") final Integer projectId,
                                                @Optional final String branchOrTag,
                                                @Optional final Pagination pagination) throws IOException {
        final List<GitlabCommit> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getCommits(projectId, pagination, branchOrTag);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get commit statuses
     *
     * @param projectId ID of the project
     * @param commitHash hash of the commit
     * @param pagination pagination
     * @return list of Gitlab commit statuses
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabCommitStatus> getCommitStatuses(@FriendlyName("Project ID") final Integer projectId,
                                                      final String commitHash, @Optional final Pagination pagination)
            throws IOException {
        final GitlabProject project;
        final List<GitlabCommitStatus> result;

        LOGGER.trace("");
        try {
            project = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (project == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getCommitStatuses(project, commitHash,
                    (pagination == null) ? new Pagination() : pagination);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get deploy keys
     *
     * @param projectId ID of the project
     * @return list of Gitlab SSH keys
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabSSHKey> getDeployKeys(@FriendlyName("Project ID") final Integer projectId) throws IOException {
        final List<GitlabSSHKey> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getDeployKeys(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get file archive
     *
     * @param projectId ID of the project
     * @return bytes of the file
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public byte[] getFileArchive(@FriendlyName("Project ID") final Integer projectId) throws IOException {
        final GitlabProject project;
        final byte[] result;

        LOGGER.trace("");
        try {
            project = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (project == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getFileArchive(project);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get group
     *
     * @param groupId ID of the group
     * @return Gitlab group
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabGroup getGroup(@FriendlyName("Group ID") final Integer groupId) throws IOException {
        final GitlabGroup result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getGroup(groupId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get group members
     *
     * @param groupId ID of the group
     * @return list of Gitlab group members
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabGroupMember> getGroupMembers(@FriendlyName("Group ID") final Integer groupId) throws IOException {
        final List<GitlabGroupMember> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getGroupMembers(groupId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get group projects
     *
     * @param groupId ID of the group
     * @return list of Gitlab project
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabProject> getGroupProjects(@FriendlyName("Group ID") final Integer groupId) throws IOException {
        final List<GitlabProject> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getGroupProjects(groupId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get groups
     *
     * @return list of Gitlab groups
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabGroup> getGroups() throws IOException {
        final List<GitlabGroup> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getGroups();
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get issue
     *
     * @param projectId ID of the project
     * @param issueId ID of the issue
     * @return Gitlab issue
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabIssue getIssue(@FriendlyName("Project ID") final Integer projectId,
                                @FriendlyName("Issue ID") final Integer issueId) throws IOException {
        final GitlabIssue result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getIssue(projectId, issueId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get issues
     *
     * @param projectId ID of the project
     * @return list of Gitlab issues
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabIssue> getIssues(@FriendlyName("Project ID") final Integer projectId) throws IOException {
        final GitlabProject project;
        final List<GitlabIssue> result;

        LOGGER.trace("");
        try {
            project = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (project == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getIssues(project);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get labels
     *
     * @param projectId ID of the project
     * @return list of Gitlab labels
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabLabel> getLabels(@FriendlyName("Project ID") final Integer projectId) throws IOException {
        final List<GitlabLabel> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getLabels(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get last commits
     *
     * @param projectId ID of the project
     * @param branchOrTag name of the branch or tag
     * @return list of Gitlab commits
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabCommit> getLastCommits(@FriendlyName("Project ID") final Integer projectId,
                                             @Optional final String branchOrTag) throws IOException {
        final List<GitlabCommit> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getLastCommits(projectId, branchOrTag);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get merge request
     *
     * @param projectId ID of the project
     * @param mergeRequestId ID of the merge request
     * @return Gitlab merge request
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabMergeRequest getMergeRequest(@FriendlyName("Project ID") final Integer projectId,
                                              @FriendlyName("Merge request ID") final Integer mergeRequestId)
            throws IOException {
        final GitlabMergeRequest result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getMergeRequestByIid(projectId, mergeRequestId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get merge request changes
     *
     * @param projectId ID of the project
     * @param mergeRequestId ID of the merge request
     * @return Gitlab merge request
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabMergeRequest getMergeRequestChanges(@FriendlyName("Project ID") final Integer projectId,
                                                     @FriendlyName("Merge request ID")final Integer mergeRequestId)
            throws IOException {
        final GitlabMergeRequest result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getMergeRequestChanges(projectId, mergeRequestId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get merge requests
     *
     * @param projectId ID of the project
     * @return list of Gitlab merge requests
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabMergeRequest> getMergeRequests(@FriendlyName("Project ID") final Integer projectId)
            throws IOException {
        final List<GitlabMergeRequest> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getMergeRequests(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Git milestones
     *
     * @param projectId ID of the project
     * @return list of Gitlab milestone
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabMilestone> getMilestones(@FriendlyName("Project ID") final Integer projectId) throws IOException {
        final List<GitlabMilestone> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getMilestones(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get namespace members
     *
     * @param namespaceId ID of the namespace
     * @return list of Gitlab project members
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabProjectMember> getNamespaceMembers(@FriendlyName("Namespace ID") final Integer namespaceId)
            throws IOException {
        final List<GitlabProjectMember> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getNamespaceMembers(namespaceId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get note
     *
     * @param projectId  ID of the project
     * @param mergeRequestId ID of the merge request
     * @param noteId ID of the note
     * @return Gitlab note
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabNote getNote(@FriendlyName("Project ID") final Integer projectId,
                              @FriendlyName("Merge request ID") final Integer mergeRequestId,
                              @FriendlyName("Note ID") final Integer noteId) throws IOException {
        final GitlabMergeRequest mergeRequest;
        final GitlabNote result;

        LOGGER.trace("");
        try {
            mergeRequest = this.config.getApiHandler().getMergeRequestByIid(projectId, mergeRequestId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (mergeRequest == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getNote(mergeRequest, noteId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get merge request notes
     *
     * @param projectId ID of the project
     * @param mergeRequestId ID of the merge request
     * @return list of the Gitlab notes
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabNote> getMergeRequestNotes(@FriendlyName("Project ID") final Integer projectId,
                                                 @FriendlyName("Merge request ID") final Integer mergeRequestId)
            throws IOException {
        final GitlabMergeRequest mergeRequest;
        final List<GitlabNote> result;

        LOGGER.trace("");
        try {
            mergeRequest = this.config.getApiHandler().getMergeRequestByIid(projectId, mergeRequestId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (mergeRequest == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getNotes(mergeRequest);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get issue notes
     *
     * @param projectId ID of the project
     * @param issueId ID of the issue
     * @return list of Gitlab notes
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabNote> getIssueNotes(@FriendlyName("Project ID") final Integer projectId,
                                          @FriendlyName("Issue ID") final Integer issueId) throws IOException {
        final GitlabIssue issue;
        final List<GitlabNote> result;

        LOGGER.trace("");
        try {
            issue = this.config.getApiHandler().getIssue(projectId, issueId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (issue == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getNotes(issue);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get open merge requests
     *
     * @param projectId ID of the project
     * @return list of Gitlab merge requests
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabMergeRequest> getOpenMergeRequests(@FriendlyName("Project ID") final Integer projectId)
            throws IOException {
        final List<GitlabMergeRequest> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getOpenMergeRequests(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get project
     *
     * @param projectId ID of the project
     * @return Gitlab project
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabProject getProject(@FriendlyName("Project ID") final Integer projectId) throws IOException {
        final GitlabProject result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

//    @Processor
//    public GitlabBuild getProjectBuild(final Integer projectId, final Integer buildId) throws IOException {
//        return this.config.getApiHandler().getProjectBuild(projectId, buildId);
//    }

    /**
     * Get project builds
     *
     * @param projectId ID of the project
     * @return list of Gitlab builds
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabBuild> getProjectBuilds(@FriendlyName("Project ID") final Integer projectId) throws IOException {
        final List<GitlabBuild> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getProjectBuilds(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get project hook
     *
     * @param projectId ID of the project
     * @param hookId ID of the hook
     * @return Gitlab project hook
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabProjectHook getProjectHook(@FriendlyName("Project ID") final Integer projectId,
                                            @FriendlyName("Hook ID") final String hookId) throws IOException {
        final GitlabProject project;
        final GitlabProjectHook result;

        LOGGER.trace("");
        try {
            project = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (project == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getProjectHook(project, hookId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get project hooks
     *
     * @param projectId ID of the project
     * @return list of Gitlab project hook
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabProjectHook> getProjectHooks(@FriendlyName("Project ID") final Integer projectId)
            throws IOException {
        final List<GitlabProjectHook> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getProjectHooks(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get project members
     *
     * @param projectId ID of the project
     * @return list of Gitlab project members
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabProjectMember> getProjectMembers(@FriendlyName("Project ID") final Integer projectId)
            throws IOException {
        final List<GitlabProjectMember> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getProjectMembers(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get projects
     *
     * @return list of Gitlab projects
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabProject> getProjects() throws IOException {
        final List<GitlabProject> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getProjects();
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get projects via sudo
     *
     * @param userId ID of the sudo user
     * @return list of Gitlab projects
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabProject> getProjectsViaSudo(@FriendlyName("User ID") final Integer userId) throws IOException {
        final GitlabUser user;
        final List<GitlabProject> result;

        LOGGER.trace("");
        try {
            user = this.config.getApiHandler().getUser(userId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (user == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");


        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getProjectsViaSudo(user);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get raw blob content
     *
     * @param projectId ID of the project
     * @param commitHash hash of the commit
     * @return bytes of the blob
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public byte[] getRawBlobContent(@FriendlyName("Project ID") final Integer projectId, final String commitHash)
            throws IOException {
        final GitlabProject project;
        final byte[] result;

        LOGGER.trace("");
        try {
            project = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (project == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getRawBlobContent(project, commitHash);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get raw file content
     *
     * @param projectId ID of the project
     * @param filePath path to the file
     * @param commitHash hash of the commit
     * @return bytes of the file
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public byte[] getRawFileContent(@FriendlyName("Project ID") final Integer projectId, final String filePath,
                                    @Optional final String commitHash) throws IOException {
        final GitlabProject project;
        final byte[] result;

        LOGGER.trace("");
        try {
            project = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (project == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getRawFileContent(project, commitHash, filePath);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get repository tree
     *
     * @param projectId ID of the project
     * @param path tree path
     * @param refName name of the reference
     * @return list of Gitlab repository tree
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabRepositoryTree> getRepositoryTree(@FriendlyName("Project ID") final Integer projectId,
                                                        @Optional final String path, @Optional final String refName)
            throws IOException {
        final GitlabProject project;
        final List<GitlabRepositoryTree> result;

        LOGGER.trace("");
        try {
            project = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (project == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getRepositoryTree(project, path, refName);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get SSH key
     *
     * @param keyId ID of the key
     * @return Gitlab SSH key
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor(friendlyName = "Get SSH Key")
    public GitlabSSHKey getSSHKey(@FriendlyName("Key ID") final Integer keyId) throws IOException {
        final GitlabSSHKey result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getSSHKey(keyId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get SSH keys
     *
     * @param userId ID of the user
     * @return list of Gitlab SSH keys
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor(friendlyName = "Get SSH Keys")
    public List<GitlabSSHKey> getSSHKeys(@FriendlyName("User ID") final Integer userId) throws IOException {
        final List<GitlabSSHKey> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getSSHKeys(userId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get system hooks
     *
     * @return list of Gitlab system hook
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public List<GitlabSystemHook> getSystemHooks() throws IOException {
        final List<GitlabSystemHook> result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getSystemHooks();
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

//    @Processor
//    public List<GitlabTag> getTags(final Integer projectId) throws IOException {
//        return this.config.getApiHandler().getTags(projectId);
//    }

    /**
     * Get current user
     *
     * @return Gitlab user
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabUser getCurrentUser() throws IOException {
        final GitlabUser result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getUser();
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get user
     *
     * @param userId ID of the user
     * @return Gitlab user
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabUser getUser(@FriendlyName("User ID") final Integer userId) throws IOException {
        final GitlabUser result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getUser(userId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Get user via sudo
     *
     * @param username users username
     * @return Gitlab user
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabUser getUserViaSudo(final String username) throws IOException {
        final GitlabUser result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().getUserViaSudo(username);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Protect branch
     *
     * @param projectId ID of the project
     * @param branchName name of the branch
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public void protectBranch(@FriendlyName("Project ID") final Integer projectId, final String branchName)
            throws IOException {
        final GitlabProject project;

        LOGGER.trace("");
        try {
            project = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (project == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        LOGGER.trace("");
        try {
            this.config.getApiHandler().protectBranch(project, branchName);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        LOGGER.debug("");
    }

    /**
     * Transfer
     *
     * @param projectId ID of the project
     * @param namespaceId ID of the namespace
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public void transfer(@FriendlyName("Project ID") final Integer projectId,
                         @FriendlyName("Namespace ID") final Integer namespaceId) throws IOException {
        LOGGER.trace("");
        try {
            this.config.getApiHandler().transfer(namespaceId, projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        LOGGER.debug("");
    }

    /**
     * Unblock user
     *
     * @param userId ID of the user
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public void unblockUser(@FriendlyName("User ID") final Integer userId) throws IOException {
        LOGGER.trace("");
        try {
            this.config.getApiHandler().unblockUser(userId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        LOGGER.debug("");
    }

    /**
     * Unprotect branch
     *
     * @param projectId ID of the project
     * @param branchName name of the branch
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public void unprotectBranch(@FriendlyName("Project ID") final Integer projectId, final String branchName)
            throws IOException {
        final GitlabProject project;

        LOGGER.trace("");
        try {
            project = this.config.getApiHandler().getProject(projectId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (project == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        LOGGER.trace("");
        try {
            this.config.getApiHandler().unprotectBranch(project, branchName);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        LOGGER.debug("");
    }

    /**
     * Update label
     *
     * @param projectId ID of the project
     * @param oldName old label's name
     * @param newName new label's name
     * @param newColor new label's color
     * @return Gitlab label
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabLabel updateLabel(@FriendlyName("Project ID") final Integer projectId, final String oldName,
                                   @Optional final String newName, @Optional final String newColor) throws IOException {
        final GitlabLabel result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().updateLabel(projectId, oldName, newName, newColor);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Update merger request
     *
     * @param projectId ID of the project
     * @param mergeRequestId ID of the merge request
     * @param targetBranch merge request target branch
     * @param assigneeId ID of the assignee
     * @param title title of the merge request
     * @param description description of the merge request
     * @param stateEvent state event of the merge request
     * @param labels labels of the merge request
     * @return Gitlab merge request
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabMergeRequest updateMergeRequest(@FriendlyName("Project ID") final Integer projectId,
                                                 @FriendlyName("Merge request ID") final Integer mergeRequestId,
                                                 @Optional final String targetBranch,
                                                 @FriendlyName("Assignee ID") @Optional final Integer assigneeId,
                                                 @Optional final String title, @Optional final String description,
                                                 @Optional final String stateEvent, @Optional final String labels)
            throws IOException {
        final GitlabMergeRequest result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().updateMergeRequest(projectId, mergeRequestId, targetBranch, assigneeId,
                    title, description, stateEvent, labels);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Update milestone
     *
     * @param projectId ID of the project
     * @param milestoneId ID of the milestone
     * @param title title of the milestone
     * @param description description of the milestone
     * @param date date of the milestone
     * @param stateEvent state event of the milestone
     * @return Gitlab milestone
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabMilestone updateMilestone(@FriendlyName("Project ID") final Integer projectId,
                                           @FriendlyName("Milestone ID") final Integer milestoneId,
                                           @Optional final String title, @Optional final String description,
                                           @Optional final org.mule.api.el.datetime.Date date,
                                           @Optional final String stateEvent) throws IOException {
        final LocalDate localDate = LocalDate.of(date.getYear(), date.getMonth() - 1, date.getDayOfMonth());
        final GitlabMilestone result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().updateMilestone(projectId, milestoneId, title, description,
                    Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()), stateEvent);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Update merge request note
     *
     * @param projectId ID of the project
     * @param mergeRequestId ID of the merge request
     * @param noteId ID of the note
     * @param body body of the note
     * @return Gitlab note
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabNote updateMergeRequestNote(@FriendlyName("Project ID") final Integer projectId,
                                             @FriendlyName("Merge request ID")final Integer mergeRequestId,
                                             @FriendlyName("Note ID") final Integer noteId, @Optional final String body)
            throws IOException {
        final GitlabMergeRequest mergeRequest;
        final GitlabNote result;

        LOGGER.trace("");
        try {
            mergeRequest = this.config.getApiHandler().getMergeRequestByIid(projectId, mergeRequestId);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (mergeRequest == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().updateNote(mergeRequest, noteId, body);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }

    /**
     * Update project
     *
     * @param projectId ID of the project
     * @param projectName name of the project
     * @param description description of the project
     * @param issuesEnabled should be issues enabled?
     * @param wallEnabled should be wall enabled?
     * @param mergeRequestsEnabled should be merge requests enabled?
     * @param wikiEnabled should be wiki enabled?
     * @param snippetsEnabled should be snippets enabled?
     * @param publik should be project public?
     * @param visibilityLevel visibility level of the project
     * @return Gitlab project
     * @throws IOException on error with connecting to the Gitlab
     */
    @Processor
    public GitlabProject updateProject(@FriendlyName("Project ID") final Integer projectId,
                                       @Optional final String projectName, @Optional final String description,
                                       @Optional final Boolean issuesEnabled, @Optional final Boolean wallEnabled,
                                       @Optional final Boolean mergeRequestsEnabled,
                                       @Optional final Boolean wikiEnabled, @Optional final Boolean snippetsEnabled,
                                       @FriendlyName("Public") @Optional final Boolean publik,
                                       @Optional final Integer visibilityLevel) throws IOException {
        final GitlabProject result;

        LOGGER.trace("");
        try {
            result = this.config.getApiHandler().updateProject(projectId, projectName, description, issuesEnabled,
                    wallEnabled, mergeRequestsEnabled, wikiEnabled, snippetsEnabled, publik, visibilityLevel);
        }
        catch (final IOException ex) {
            LOGGER.error("");
            throw ex;
        }
        if (result == null) {
            LOGGER.error("");
            throw new IOException();
        }
        LOGGER.debug("");

        return result;
    }
}
