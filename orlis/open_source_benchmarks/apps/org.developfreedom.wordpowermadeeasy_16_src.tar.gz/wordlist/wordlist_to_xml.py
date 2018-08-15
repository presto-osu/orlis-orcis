#!/usr/bin/env python3
#
#   wordlist-to-xml.py - Generate XML file from word list
#   Copyright (c) 2014 Shubham Chaudhary <me@shubhamchaudhary.in>
#
#   This program is free software: you can redistribute it and/or modify
#   it under the terms of the GNU General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   This program is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU General Public License for more details.
#
#   You should have received a copy of the GNU General Public License
#   along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

import sys

# These are the words/patterns that exist at the
# begining of useless lines in txt files
USELESS_WORDLIST_SHORT = [
        '\n',
        'Notes',
        'Page ',
        'Group',
        ]
USELESS_WORDLIST_LONG = [
        'More wo',
        '\x0cGroup ',
        ]

def main(argv):
    argc = len(argv)
    if argc != 2:
        print('Aborting! Supply filename.')
        return
    # Open text file created from pdf using pdftotext
    filename = argv[1]
    fhan = open(filename, 'rU')
    data = fhan.readlines()
    # Create dictionary of words read from txt files
    word_pair_list = []    # We need list of dicts below to xmlify
    current_dict = {}
    i = 0
    while i < len(data):
        line = data[i]
        if not (line[:5] in USELESS_WORDLIST_SHORT
                or line[:7] in USELESS_WORDLIST_LONG):
            if line[:5] != '.....':
                # Make dict
                if current_dict == {}:
                    current_dict['word'] = line[:-1]
                else:
                    # Handle multi line meaning
                    out = ''
                    while line[:5] == '\n':
                        print('line: ', line)
                        line.replace('\n', ' ')
                        out += line
                        i += 1
                        line = data[i]
                    out = out + '\n'
                    current_dict['meaning'] = line[:-1]
            else:
                # Append to list of dicts
                word_pair_list.append(current_dict)
                current_dict = {}
        i += 1
    return list_to_xml(word_pair_list)

def list_to_xml(list_of_dicts):
    ''' Create XML from python dictionary '''
    from dict2xml import dict2xml as xmlify
    xml_out = xmlify(list_of_dicts, wrap='pair', indent=' ')
    print(xml_out)
    return xml_out


if __name__ == '__main__':
    main(sys.argv)
