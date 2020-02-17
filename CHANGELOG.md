#Recap Plugin Changelog
  
### 1.0.0 - First release
#### Introduction:
The recap plugin was made to facilitate quick messages for information hand-off between
 moderators of a server. Logs are saved in a simple text file to keep it human readable.
 Configure your preferences in the config.yml and get started!

#### Commands:
* /recap - Display the last X recap messages
* /recap <message...> - Add a new recap to the log
* /recap reload - Reload the Recap plugin, requires console access or the recap.reload permission

#### Permissions:
* recap.use: Permission to access the /recap command - default: op
* recap.notify: Permission to receive notifications of recap changes - default: op
* recap.reload: Permission to reload the Recap plugin - default: false
* recap.reload.notify: Permission to receive notification on Recap plugin reload - default: op

#### Compatibility:
* Built for 1.13+
* Tested in 1.14 and 1.15

