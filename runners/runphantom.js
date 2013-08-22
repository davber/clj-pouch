#!/usr/bin/env phantomjs

// reusable phantomjs script for running clojurescript.test tests
// see http://github.com/cemerick/clojurescript.test for more info

var p = require('webpage').create();
var sys = require('system');
p.settings.webSecurityEnabled = false;
p.settings.localToRemoteUrlAccessEnabled = true;
p.onConsoleMessage = function (x) {
    var line = x;
    if (line !== "[NEWLINE]")
        console.log(line.replace(/\[NEWLINE\]/g, "\n"));
};
p.open("about:blank");
p.injectJs(sys.args[1]);

