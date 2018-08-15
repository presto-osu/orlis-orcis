!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!![]&
  []==[]==[]        ||      []*[]                                              &
      []          ||  []    ||  []                                             &
      []         ||!![]||   !![]                                               &
      []        ||      !!  []                                                 &
      []       ||        !! []                                                 &

  []==[]==[]    ||    !!    []*!![]                                            &
      []        ||    !!    []                                                 &
      []        ||!![]||    []||[]                                             &
      []        ||    !!    []                                                 &
      []        ||    !!    []*!![]                                            &

  []==[]        ||                  []             ||!![]   ||    ![]          &
  []    ||      []                ||  []         &[]        ||   []            &
  []    ||      []               ||    !!       []          || []              &
  []||!![]      ||              !!      []     ||           !![]               &
  []    ||      []             ||!![]||!![]     ||          [] &[]             &
  []    ||      []            ||          !!     ![]        ||   []            &
  []||[]        ||!![]||[]   ||            []      ||![]&   !!    ![]          &

  []==[]==[]    &&[]*![]&&   []         ||[]&[]    ||![]                       &
      []            ||       []         ||       []                            &
      []            ||       []         ||[]&[]   &[]||[]                      &
      []            ||       []         ||              []                     &
      []        &&[]||[]&&   []&[]*[]   ||[]&!!    ![]&[]                      &
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!![]

var mode, mode_name, mode_parent, last_focus = false, time_delay = 0;
var touch_event = 'click';
var game_over = false; // Used to play with a keyboard

$(document).ready(function() {

    load_storage();

    if(storage.vibrate === undefined) {
        migrate_v1_1(); // TODO : remove this line in 2016
        storage.vibrate = true;
        save_storage();
    }

    set_vibrate(storage.vibrate);

    $('#restart, #game-over, #quit, #new-high-score').hide();

    if(navigator.userAgent.indexOf('Mobile') !== -1) {
        touch_event = 'touchstart';
    } else {
        FastClick.attach(document.body);
    }

    mode_name = window.location.search.substr(1);

    if(mode_name.indexOf('/') !== -1) {
        mode_parent = mode_name.split('/')[1];
        mode_name = mode_name.split('/')[0];
    }

    if(mode_name in modes) {

        Keyboard.setup();

        if(window.droid) {
            // XXX : Beware : here lies the most terrible Android WebView bugfix in the history of the world
            var height = Math.round($(window).width() * window.droid.getSize());
            // This feels dirty
            $('html, body, #game-over').height(height);
            // Even joking about it in the comments doesn't makes me feel better about myself
            $('#restart, #quit').css('top', 0.8 * height);
            // brb, I'm gonna be sick
        }

        mode = modes[mode_name];

        if(mode_parent && 'parents' in mode && mode.parents.indexOf(mode_parent) !== -1) {
            mode.parent = mode_parent;
        }

        $('title').text($('title').text() + ' - ' + mode_name.ucfirst());

        // Pause time on lost focus
        window.onblur = function() {
            last_focus = window.performance.now();
        }

        window.onfocus = function() {
            if(last_focus) {
                time_delay += window.performance.now() - last_focus;
                last_focus = false;
            }
        }

        mode_inheritance(mode);

        // XXX : Bugfix for Firefox OS
        setTimeout(mode.init, 100);
    } else {
        // Select a mode
        var html = '<div class="select-mode"><h1>Tap the Black Tiles<br /><small>Select a mode...</small></h1>';
        for(mode in filter_object(modes, /^[^_]/)) {
            var info_html = '<br/><small class="high-score"></small>';
            var parents_html = "";

            modes[mode].parents.forEach(function(p) {
                var high_score = storage['score.' + mode + '.' + p] || 0;

                parents_html += '<a href="?' + mode + '/' + p + '">'
                    + p.replace(/_/g, ' ')
                    + ' <br /><small class="high-score">Best : ' + high_score + '</small>'
                    + '</a>';
            });

            html += '<div href="#!" onclick="select_mode(this, \'' + mode + '\')">'
                + '<i class="name">' + mode.replace(/_/g, ' ') + '</i>'
                + info_html
                + parents_html
                + '</div>';
        }
        html += '<div onclick="select_mode(this, \'_random\')"><i class="name">Random</i><br /></div>';

        html += '<div onclick="select_mode(this, null)"><br /><i class="name">More...</i><br />'
            + '<a href="about.html">About this game</a>'
            + '<a href="high-scores.html">High Scores</a>'
            + '<a href="#!" onclick="toggle_vibration(this)">[' + (storage.vibrate ? 'x' : ' ' )+ '] Toggle vibration</a>'
            + '</div>';
        html += '</div>';

        $('html').addClass('menu')
        $('body').addClass('menu').html(html);
        $('.select-mode div a').fadeTo(0, 0);
    }
});

/* The functions to handle the game are defined for each mode
 * in this JSON object. For instance, modes._base.init contains
 * the function to execute when the game starts. Note that the
 * standard functions for a mode are init, append, move, speedUp,
 * tap and lost, but the only required function is init, since
 * it is called when the document is ready.
 *
 * The modes are fetched from here in the "Select a mode" section.
 */
var modes = {
    // Non-playable mode with a common base for all the other modes
    _base: {
        init: function() {
            mode.body_height = $('body').height();
            mode.score = 0;
            mode.last_move = time();
            mode.scroll_top = 0;
            console.log(mode_name, mode_parent);
            mode.append();
            mode.row_height = $('div').height();
            mode.speed = mode.row_height*2;
        },
        generate_tiles: function() {
            return [
                '<span></span>',
                '<span></span>',
                '<span></span>',
                '<span class="black"></span>'
            ].shuffle();
        },
        append: function() {
            var tiles = mode.generate_tiles();
            $('body').children().first().before('<div>' + tiles.join('') + '</div>');
            $('body div').first().children().bind(touch_event, function() { tap(this) });
        },
        validate_tap: function(context) {
            if(hasClass(context, 'black')) {
                return 'good';
            } else if(!hasClass(context, 'gray')) {
                return 'bad';
            }
            return 'ignore';
        },
        // We always need more callbacks
        tap_callbacks_good: function(context) {
            context.classList.remove('black');
            context.classList.add('gray');
            mode.score++;
            document.getElementById('score').textContent = mode.score;
        },
        tap_callbacks_bad: function(context){
            $(context).addClass('red');
            mode.move = function() {};
            mode.lost();
        },
        tap_callbacks_ignore: function() {},
        tap: function(context) {
            var tap_value = mode.validate_tap(context);
            mode['tap_callbacks_' + tap_value](context);
            vibrate();
        },
        death_condition: function(context) {
            return $(context).children('.black').length;
        },
        lost: function() {
            $('span').unbind(touch_event);
            $('#final-score').text(mode.score);

            var item = 'score.' + mode_name + '.' + mode.parent;
            var current_high_score = storage[item] || 0;

            if(current_high_score < mode.score) {
                $('#new-high-score').show();
                storage[item] = mode.score;
                save_storage();
            }

            game_over = 1;

            $('#high-score').text(storage[item] || 0);
            $('#game-over').fadeIn(1000);
            $('#restart, #quit').delay(400).fadeIn('600');
        },
        freeze: function(timeout) {
            var speed = mode.speed;
            var tap = mode.tap;
            var speedUp = mode.speedUp;
            mode.speed = 0;
            mode.tap = function() {};
            mode.speedUp = function() {};
            setTimeout(function() {
                mode.speed = speed;
                mode.tap = tap;
                mode.speedUp = speedUp;
            }, timeout);
        },
    },
    _arcade: {
        parent: '_base',
        init: function() {
            parent('_arcade').init();
            mode.move();
        },
        append: function() {
            parent('_arcade').append();
            mode.speedUp();
        },
        move: function() {
            if(!time()) {
                setTimeout(mode.move, 3);
                return;
            }

            var delta_y = (time() - mode.last_move)/1000 * mode.speed;
            mode.last_move = time();

            mode.scroll_top = (mode.scroll_top + delta_y);
            if(mode.scroll_top >= mode.row_height) {
                mode.append();
                mode.scroll_top = 0;
            }

            mode.check_death();

            setTimeout(mode.move, 40);
        },
        missed_tile_red: function(last_row) {
            $(last_row).children('.black').addClass('red');
        },
        check_death: function() {
            $('div').css({
                top: mode.scroll_top - mode.row_height + 'px'
            }).last().each(function() {
                if($(this).position().top > mode.body_height) {
                    if(mode.death_condition(this)) {
                        mode.missed_tile_red(this);
                        mode.move = function() {};
                        $('div').animate({
                            top: (-2*mode.row_height) + 'px'
                        }, 1000, function() {
                            mode.lost();
                        });
                    } else {
                        $(this).remove();
                        Keyboard.last_unclicked_row--;
                    }
                }
            });
        },
        speedUp: function() {
            mode.speed += mode.row_height*0.05;
        }
    },
    _zen: {
        parent: '_base',
        init: function() {
            parent('_zen').init();
            mode.append();
            mode.append();
            mode.speed = mode.row_height;
        },
        move: function() {
            mode.append();
            $('div').css({
                top: (-mode.row_height) + 'px'
            }).animate({
                top: '0px'
            }, 100);

            $('div').each(function() {
                if($(this).position().top >= mode.body_height) {
                    if(mode.death_condition(this)) {
                        $('div').animate({
                            top: (-mode.row_height*2) + 'px'
                        }, 1000, function() {
                            mode.lost();
                        });
                        $(this).children('.black').removeClass('black').addClass('red');
                        mode.lost();
                    } else {
                        $(this).remove();
                        Keyboard.last_unclicked_row--;
                    }
                }
            });
        },
        validate_tap: function(context) {
            if(hasClass(context, 'black') && !$(context).parent().next('div').find('.black').length) {
                return 'good';
            }
            return 'bad';
        },
        tap_callbacks_good: function(context) {
            parent('_zen').tap_callbacks_good(context);
            mode.move();
        }
    },
    _stamina: {
        parent: '_arcade',
        speedUp: function() {},
    },
    _random_speed: {
        parent: '_arcade',
        speedUp: function() {
            mode.speed = mode.row_height * (Math.random() * 5) + mode.row_height/2;
        },
    },
    classic: {
        parents: ['_arcade', '_stamina', '_zen', '_random_speed'],
    },
    // TODO : Replace by a level system (slow, normal, fast)
    faster: {
        parents: ['_arcade', '_stamina'],
        init: function() {
            parent('faster').init();
            mode.speed = mode.row_height*3.3;
        },
        speedUp: function() {
            mode.speed += mode.row_height*0.15;
        },
    },
    flash: {
        parents: ['_arcade', '_stamina', '_zen', '_random_speed'],
        init: function() {
            parent('flash').init();
            mode.flash();
        },
        invert: 3,
        append: function() {
            parent('flash').append();
            mode.flash();
        },
        flash: function() {
            if(mode.invert == 3) {
                $('span').css({backgroundColor: '#070707'});
            } else {
                $('span').css({backgroundColor: 'transparent'});
            }
            mode.invert++;
            mode.invert %= 4;
        }
    },
    /*sprint: {
        // TODO : You've got 15 seconds to tap all the black tiles you can
    },*/
    double: {
        parents: ['_arcade', '_stamina'],
        append: function() {
            parent('double').append();
            var double = false;
            $('div:eq(0) span:not("black")').each(function() {
                if(!double && Math.random() < 0.05) {
                    $(this).addClass('black');
                    double = true;
                }
            });
        },
    },
    triple: {
        parents: ['_arcade', '_stamina'],
        generate_tiles: function() {
            return [
                '<span></span>',
                '<span class="black"></span>',
                '<span class="black"></span>',
                '<span class="black"></span>'
            ].shuffle();
        },
    },
    trap: {
        parents: ['_arcade', '_stamina'],
        generate_tiles: function() {
            return [
                '<span></span>',
                '<span></span>',
                '<span></span>',
                Math.random() > 0.25 ? '<span class="black"></span>' : '<span class="trap"><br />/!\\</span>'
            ].shuffle();
        },
    },
    twice: {
        parents: ['_arcade', '_stamina'],
        generate_tiles: function() {
            return [
                '<span></span>',
                '<span></span>',
                '<span></span>',
                Math.random() > 0.25 ? '<span class="black"></span>' : '<span class="twice black"><br/>2x</span>'
            ].shuffle();
        },
        validate_tap: function(context) {
            if(hasClass(context, 'twice')) {
                Keyboard.last_unclicked_row--;
                return 'twice';
            }
            return parent('twice').validate_tap(context);
        },
        tap_callbacks_twice: function(context) {
            $(context).removeClass('twice').text('');
        },
    },
    bastard: {
        parents: ['_arcade', '_stamina'],
        append: function() {
            parent('bastard').append();
            var bastard = false;
            $('div:nth-child(1) span, div:nth-child(2) span').each(function() {
                if(!bastard && Math.random() < 0.15) {
                    $(this).addClass('black');
                    bastard = true;
                }
            });
        },
    },
    hardcore: {
        parents: ['_arcade', '_stamina'],
        generate_tiles: function() {
            return [
                '<span class="black"></span>',
                '<span class="black"></span>',
                '<span class="black"></span>',
                '<span class="black"></span>'
            ];
        },
    },
    disco: {
        parents: ['_arcade', '_stamina', '_zen', '_random_speed'],
        append: function() {
            parent('disco').append();
            var that = this;
            $('span').each(function() {
                $(this).css({
                    backgroundColor: that.colors.choose(),
                });
            });
        },
        colors: ['#00C', '#0CC', '#0C0', '#CC0', '#C0C', '#C00']
    },
    loop: {
        parents: ['_arcade', '_stamina', '_zen', '_random_speed'],
        init: function() {
            mode.loop = [0, 1, 2, 3, 0, 1, 2, 3].shuffle();
            parent('loop').init();
        },
        generate_tiles: function() {
            var tiles = [];
            var next = this.next();
            for(var i = 0; i<4; i++) {
                tiles.push(i == next ? '<span class="black"></span>' : '<span></span>');
            }
            return tiles;
        },
        append: function() {
            parent('loop').append();
            this.row++;
        },
        row: 0,
        next: function() {
            return mode.loop[mode.row % mode.loop.length];
        }
    },
    odd_numbers: {
        parents: ['_arcade', '_stamina'],
        generate_tiles: function() {
            var number = rand_int(0, 100);
            return [
                '<span></span>',
                '<span></span>',
                '<span></span>',
                '<span class="black' + (number & 1 ? ' good' : '') + '"><br />' + number + '</span>'
            ].shuffle();
        },
        death_condition: function(last_row) {
            return $(last_row).children('.good').not('.gray').length;
        },
        validate_tap: function(context) {
            if(hasClass(context, 'good') && !hasClass(context, 'gray')) {
                return 'good';
            }
            return parent('odd_numbers').validate_tap(context);
        },
        tap_callbacks_good: function(context) {
            parent('odd_numbers').tap_callbacks_good(context);
            context.textContent = '';
        },
    },
    annoying_circle: {
        parents: ['_arcade', '_stamina', '_zen', '_random_speed'],
        init: function() {
            parent('annoying_circle').init();
            $('body').children().first().before('<p id="blocking-circle"></p>');
            mode.move_circle();
        },
        move_circle: function() {
            $('#blocking-circle').animate({
                top: rand_int(0, 0.8 * mode.body_height) + 'px',
                left: rand_int(0, $('body').width()/2) + 'px',
            }, 1200, mode.move_circle);
        }
    },
    no_feedback: {
        parents: ['_arcade', '_stamina', '_zen', '_random_speed'],
        init: function() {
            parent('no_feedback').init();
            vibrate = function() {};
        },
        death_condition: function(last_row) {
            return $(last_row).children('.black').not('.tapped').length;
        },
        validate_tap: function(context) {
            if(hasClass(context, 'black') && !hasClass(context, 'tapped')) {
                return 'good';
            }
        },
        tap_callbacks_good: function(context) {
            parent('no_feedback').tap_callbacks_good(context);
            document.getElementById('score').textContent = '0';
            context.classList.remove('gray');
            context.classList.add('black');
            context.classList.add('tapped');
        }
    },
    one_at_the_time: {
        parents: ['_arcade', '_stamina', '_zen', '_random_speed'],
        init: function() {
            parent('one_at_the_time').init();
            $('div').last().children().get(rand_int(0,4)).classList.add('black');
        },
        generate_tiles : function() {
            return [
                '<span></span>',
                '<span></span>',
                '<span></span>',
                '<span></span>'
            ];
        },
        validate_tap: function(context) {
            if(context.parentElement.previousElementSibling === null) {
                return 'ignore';
            }
            return parent('one_at_the_time').validate_tap(context);
        },
        tap_callbacks_good: function(context) {
            parent('one_at_the_time').tap_callbacks_good(context);
            $(context).parent().prev().children().get(rand_int(0,4)).classList.add('black');
        },
    },
    flip: {
        parents: ['_arcade', '_stamina', '_zen', '_random_speed'],
        init: function() {
            parent('flip').init();
            $('body, #score').css({
                'transition': 'transform 1s ease-in-out',
                'transitionDuration': '0.6s',
            });
            setTimeout(function() {
                mode.transform();
            }, rand_int(4000, 16000));
        },
        transform_state: -1,
        transform: function() {
            mode.freeze(600);
            $('body, #score').css({
                transform: 'scale(' + mode.transform_state + ', 1)'
            });
            mode.transform_state = -mode.transform_state;
            setTimeout(function() {
                mode.transform();
            }, rand_int(4000, 16000));
        },
        lost: function() {
            parent('flip').lost();
            mode.transform = function() {};
            $('body, #score').css({
                transform: 'scale(1, 1)',
                transitionDuration: '1s',
            });
        }
    },
    zig_zag: {
        parents: ['_arcade', '_stamina', '_zen', '_random_speed'],
        init: function() {
            parent('zig_zag').init();
            $('body, #score').css({
                transition: 'transform 1s ease-in-out',
                'transitionDuration': '0.6s',
            });
            mode.transform_state = parseInt($('body').width()/7);
            mode.transform();
        },
        transform: function() {
            $('body, #score').css({
                transform: 'translate(' + mode.transform_state + 'px)',
            });
            mode.transform_state = -mode.transform_state;
            setTimeout(function() {
                mode.transform();
            }, 1000);
        },
        lost: function() {
            parent('zig_zag').lost();
            mode.transform = function() {};
            $('body, #score').css({
                transform: 'translate(0px)',
            });
        }
    },
    scramble: {
        parents: ['_arcade', '_stamina', '_zen', '_random_speed'],
        init: function() {
            parent('scramble').init();

            setTimeout(function() {
                mode.scramble();
            }, rand_int(4000, 9000));
        },
        scramble: function() {
            mode.freeze(200);

            $('div').each(function() {
                var tile = $(this).children('.black, .gray');
                tile.remove();
                var reference_tile = $(this).children(':eq(' + rand_int(0, $(this).children().length) + ')');
                if(Math.random() < 0.5) {
                    reference_tile.before(tile);
                } else {
                    reference_tile.after(tile);
                }
                // Rebind event
                tile.bind(touch_event, function() { tap(this) });
            });

            setTimeout(function() {
                mode.scramble();
            }, rand_int(4000, 9000));
        },
        lost: function() {
            parent('scramble').lost();
            mode.scramble = function() {};
            $('body').fadeTo('fast', 1);
        }
    },
    right_color: {
        parents: ['_arcade', '_stamina', '_random_speed'],
        init: function() {
            parent('right_color').init();
            $('body').children().first().before('<p id="color-indicator"></p><p id="time-indicator"></p>');
            mode.last_color_modification = -3000;
            mode.next_color = mode.colors.choose();
            mode.change_color();
            mode.speed = mode.row_height*3;
        },
        generate_tiles: function() {
            return [
                '<span></span>',
                '<span></span>',
                '<span></span>',
                '<span class="good" style="background-color: ' + mode.colors.choose() + '"></span>'
            ].shuffle();
        },
        death_condition: function(last_row) {
            return $(last_row).children('.good[style="background-color: ' + mode.color + '"]').length
                       && $(last_row).position().top > mode.body_height - mode.row_height;
        },
        missed_tile_red: function(last_row) {
            $(last_row).children('.good').css('background-color', 'red');
        },
        validate_tap: function(context) {
            if(hasClass(context, 'good') && $(context).css('backgroundColor') == mode.color) {
                return 'good';
            }
            return parent('right_color').validate_tap(context);
        },
        tap_callbacks_good: function(context) {
                parent('right_color').tap_callbacks_good(context);
                context.classList.remove('good');
        },
        lost: function() {
            parent('right_color').lost();
            mode.change_color = function() {};
        },
        change_color: function() {
            var ts = time();
            if(mode.last_color_modification + 3000 < ts) {
                $('.good[style="background-color: ' + mode.next_color + '"]').each(function() {
                    // Tolerence for tiles that are almost outside of the screen
                    if($(this).parent().position().top > mode.body_height - 1.2 * mode.row_height) {
                        $(this).removeClass('good').addClass('gray');
                    }
                });
                mode.color = mode.next_color;
                mode.next_color = mode.colors.choose();
                $('#time-indicator').css('backgroundColor', mode.next_color);
                $('#color-indicator').css({backgroundColor: mode.color});
                mode.last_color_modification = ts;
            } else {
                var percent = Math.abs(Math.round((mode.last_color_modification - ts)/3000*100));
                $('#time-indicator').width(percent + '%');
            }
            setTimeout(mode.change_color, 30);
        },
        colors: ['rgb(255, 0, 255)', 'rgb(0, 0, 255)', 'rgb(0, 200, 0)', 'rgb(255, 255, 0)', 'rgb(0, 0, 0)'],
        color: '', // current color
        next_color: '',
        last_color_modification: 0,
    },
    mirror: {
        parents: ['_arcade', '_stamina', '_zen'],
        append: function() {
            parent('mirror').append();
            $('div').first().children().each(function(index) {
                if($($(this).parent().children().get(3 - index)).hasClass('black')) {
                    $(this).addClass('good');
                }
            });
        },
        death_condition: function(last_row) {
            return !hasClass($(last_row).children('.good')[0], 'gray');
        },
        validate_tap: function(context) {
            if(hasClass(context, 'good') && !hasClass(context, 'gray')) {
                return 'good';
            } else if(hasClass(context, 'black')) {
                return 'bad';
            }
            return parent('mirror').validate_tap(context);
        },
    },
    _random: {
        init: function() {
            var keys = [];
            for(var m in modes) {
                if(m.search(/^_/) !== 0) {
                    keys.push(m);
                }
            }
            var name = keys.shuffle()[0];
            var url = '?' + name;

            if('parents' in modes[name]) {
                url += '/' + modes[name].parents.shuffle()[0];
            }
            window.location = url;
        }
    }
};

function select_mode(context, mode) {
    if(mode && !('parents' in modes[mode])) {
        window.location = '?' + mode;
        return;
    }

    if($(context).hasClass('selected') && mode !== null) {
        $(context).removeClass('selected');
        $('a', context).fadeTo('fast', 0);
        $('.select-mode div').css({width: '50%', height: '100px'});
        return;
    }

    $('.selected a').fadeTo('fast', 0);
    $('.selected').removeClass('selected');

    var children = $(context).children('a').length;
    $(context).children('a').css('width', 100/children + '%');
    $(context).addClass('selected').css({width: '100%', height: '200px'});
    $('a', context).fadeTo('fast', 1);

    var sibiling = (($(context).index() + 1) % 2) ? $(context).prev() : $(context).next();
    sibiling.css({width: '0px', height: '200px'}).delay(0.2).css('height', 0);

    $('.select-mode div').not(context).not(sibiling).css({width: '50%', height: '100px'});
}

function tap(context) {
    if(!time()) {
        return;
    }
    mode.tap(context);
}

/**
 * Copies the parent's methods for a given `mode`. The
 * methods overloaded by `mode` are ignored.
 */
function mode_inheritance(mode) {
    // Mode inheritance
    if('parent' in mode) {
        var parent = mode.parent;
        if('parent' in modes[parent]) {
            mode_inheritance(modes[parent]);
        }
        for(var action in modes[parent]) {
            if(!(action in mode)) {
                mode[action] = modes[parent][action];
            }
        }
    }
}

/**
 * Yeah... This is where it gets *real* hackish...
 * Since this mess of callbacks never provides a coherent 'this', you have
 * to specify who's parent you need to access...
 * At least, this wrapper makes it a bit cleaner.
 */
function parent(mode) {
    return modes[modes[mode].parent];
}

function time() {
    if(last_focus) {
        return 0;
    }

    return window.performance.now() + time_delay;
}

/**
 * Gives a less awkward way to play on a computer than using the mouse
 */
Keyboard = {
    last_unclicked_row: 1,
    setup: function() {
        $('body').keydown(function(e) {

            if(e.key == ' ') {
                // Reload the game if it's game over
                game_over && window.location.reload();
                // Skip a row
                Keyboard.last_unclicked_row++;
                return;
            }

            var map = {
                h: 0, j: 1, k: 2, l: 3,
                q: 0, w: 1, e: 2, r: 3,
                a: 0, z: 1,
                u: 0, i: 1, o: 2, p: 3,
                1: 0, 2: 1, 3: 2, 4: 3
            };

            if(Keyboard.last_unclicked_row > $('div').length ||
                map[e.key] === undefined) {
                return;
            }

            $('div').eq(-Keyboard.last_unclicked_row)
                .children()
                .eq(map[e.key])
                .click();
            Keyboard.last_unclicked_row++;
        });
    },
};

var vibrate;

function set_vibrate(vib) {
    if(vib) {
        vibrate = function() {
            navigator.vibrate(50);
        };
    } else {
        vibrate = function() {};
    }
}

function toggle_vibration(context) {
    storage.vibrate = !storage.vibrate;
    save_storage();
    $(context).text('[' + (storage.vibrate ? 'x' : ' ') + '] Toggle vibration');
}

/**
 * Bundles the old localStorage values in the new simplified one
 * TODO : Remove this in 2016
 */
function migrate_v1_1() {
    var old_storage = {};
    for(var i=0; i<window.localStorage.length; i++) {
        var key = window.localStorage.key(i);
        old_storage[key] = window.localStorage.getItem(key);
    }
    storage = old_storage;
    window.localStorage.clear();
    save_storage();
}
