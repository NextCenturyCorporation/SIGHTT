<div class="fieldcontain required">
    <label for="currentEmail"><g:message code="user.current.email.label"
                                         default="Current Password"/>
    </label>
    <span id="currentEmail">${it}</span>
</div>

<div class="fieldcontain required">
    <label for="email"><g:message
            code="user.new.email.label" default="New Email"/>  <span class="required-indicator">*</span>
    </label>
    <input name="email" type="text" pattern=".+@.+\..+" required=""
           placeholder="New Email Address"/>

</div>