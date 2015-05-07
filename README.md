# ez_mixpanel

Talk with mixpanel from the server.

## Usage

```clojure
(require '[ez-mixpanel :as emp])

;; token:        Gotten from the mixpanel account panel
;; distinct-id:  The id which mixpanel tracks around. Normally it's the id in the database for a user. 
;;               distinct-id is always converted to a string
;; event:        The event to track. Always a string
;; opts:         Optional parameters to send in to mixpanel which have special significance. 
;;               Refer to mixpanel documentation. Set :time to :mixpanel if you wish for mixpanel to set the time
;;               for the event. This is for the event function only.
;; data:         Optional parameters to send in to mixpanel for the event itself.

;; optional callback for receiving the result of the interaction with mixpanel's servers
;; track [token distinct-id event opts data]
;; track [token distinct-id event opts data cb]
(emp/track "mytoken" "myuserid" "my test event" nil {:test1 "true"})


;; The following functions all operate on the profile for a user in mixpanel. And follow the same 
;; pattern for parameters to send in to the function.

;; NOTICE: opts follow a different pattern than the event for these functions

;; parameter pattern
;; function [token distinct-id opts data]
;; function [token distinct-id opts data cb]

;; set properties for the user profile. destructive updates
(emp/set "mytoken" "myuserid" nil {"$email" "my email here"})

;; set properties for the user profile. non destructive update
(emp/set-once "mytoken" "myuserid" nil {"$created" "2015-05-07 15:24:09"})

;; add numerical values to profile
(emp/add "mytoken" "myuserid" nil {"Coins earned", 4})

;; append values to a user profile
(emp/append "mytoken" "myuserid" nil {"Books read", ["Proverbs" "Song of Songs" "Ecclesiastes"]})

;; perform a union on the values of a user profile property
(emp/union "mytoken" "myuserid" nil {"Books read", ["Proverbs", "Mark"]})

;; unset properties on a user profile
(emp/unset "mytoken" "myuserid" nil ["Coins earned"])

;; delete a user profile. this is an action that cannot be undone
;; this function does not take a data argument
(emp/delete "mytoken" "myuserid" nil)



;; engage is the function which supports all the functions operation on the user profile.

;; NOTICE: opts behave differently from track. refer to mixpanel's documentation

;; engage [token distinct-id opts operation value]
;; engage [token distinct-id opts operation value cb]

;; does the same thing as emp/set in the example above
(emp/engage "mytoken" "myuserid" nil :set {"$email" "my email here"})
```

## License

Copyright © 2015 Emil Bengtsson

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
