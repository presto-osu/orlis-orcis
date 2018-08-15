var storage = {};

function load_storage() {
    storage = JSON.parse(window.localStorage.getItem('storage')) || {};
}

function save_storage() {
    window.localStorage.setItem('storage', JSON.stringify(storage));
}

function rand_int(min_rand, max_rand) {
    return parseInt(min_rand + (Math.random()*1000 % (max_rand - min_rand)));
}

Array.prototype.choose = function() {
    return this[Math.floor(Math.random() * this.length)];
};

String.prototype.ucfirst = function() {
    var string = this.split('');
    string[0] = string[0].toUpperCase();
    return string.join('');
};

// https://jsperf.com/pure-js-hasclass-vs-jquery-hasclass/40 (classListContains)
function hasClass(el, className) {
    return el.classList.contains(className);
}

// See https://jsperf.com/array-shuffle-comparator/14
Array.prototype.shuffle = function() {
    var temp, j, i = this.length;
    while (--i) {
	    j = ~~(Math.random() * (i + 1));
	    temp = this[i];
	    this[i] = this[j];
	    this[j] = temp;
    }
    return this;
};

/**
 * Returns an object containing only the keys matching `key_regex`
 */
function filter_object(source, key_regex) {
    var object = {};
    for(var key in source) {
        if(key.match(key_regex))
            object[key] = source[key];
    }
    return object;
};
