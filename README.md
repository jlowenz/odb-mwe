# odb-mwe

A minimal working example of an OrientDB bug. This bug seems to be present in the memory and plocal storage, but not during remote access to a server. 

Two vertices are created that refer to each other. They are subsequently queried; in the case of the remote server, the link fields are valid and the mutual references are in place. In the case of memory and plocal embedded DBs, this is not the case: one field is valid while the other isn't. 

## Download and test

Download the source (it's in Clojure), and run `lein midje` to run the test. 

## License

Copyright © 2015 jlowenz

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
