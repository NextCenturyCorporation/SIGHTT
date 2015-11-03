<div class="fieldcontain required">
    <label for="currentPassword"><g:message code="user.current.password.label"
                                            default="Current Password"/> <span class="required-indicator">*</span>
    </label>
    <g:passwordField name="currentPassword" required="" value="" placeholder="Current Password"/>
</div>

<div class="fieldcontain required">
    <label for="newPassword"><g:message
            code="user.new.password.label" default="New Password"/>  <span class="required-indicator">*</span>
    </label>
    <g:passwordField name="newPassword" required="" value="" placeholder="New Password"/>

</div>

<div class="fieldcontain required">
    <label for="newPassword"><g:message
            code="user.confirm.password.label" default="Retype Password"/>  <span class="required-indicator">*</span>
    </label>
    <g:passwordField name="confirmPassword" required="" value="" placeholder="Retype Password"
                     oninput="checkRetypePassword(this)"/>
    <span id="passwordStatus" role="status"></span>
</div>