## Personal playground for keeping up with om-next

I would strongly discourage anyone from following along with, what in all honesty, are probably anti-patterns.  This is strictly a project to famaliarize
myself with the various libraries used.


## Usage

* Don't.  
* If you *really really* want to run this
  * it assumes a running datomic transactor....so you'll need to set that up
  * The pedestal server lives in `om-next-todo`.  You will need to `cd om-next-todo`, `lein repl`, and type `(go)` to start it. it will run on port 8484. 
  * the clojurescript project lives in `om-next-test`.  you can run the UI by `cd om-next-test`, `run -m clojure.main script/figwheel.clj`.  This will start the UI on `localhost:3449`
  * again, I urge you to find a better example elsewhere!!

