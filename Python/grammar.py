#!/usr/bin/python3

import sys
from collections import defaultdict

class CFG:

    def __init__(self):
        self.production_rules = defaultdict(list)   #{ LHS : list of lists }, where inner lists are possible alternations}
        self.terminals = set()
        self.non_terminals = set()
        self.start_symbol = ""

    def contains_terminal(self, production):
        return any(map(lambda e: e in self.terminals or e == self.start_symbol, production))

    def contains_lambda(self, rule):
        assert rule in self.production_rules

        for production in self.production_rules[rule]:
            if len(production) is 1 and production[0] == "lambda":
                return True
        
        return False


    def first(self, rule):
        print(rule)
    
    def derives_to_lambda(self, rule):
        assert rule in self.production_rules
        
        if self.contains_lambda(rule):
            return True

        alternations = self.production_rules[rule]

        for rhs in alternations:
            # rhs's that have a terminal cannot
            # derive to lambda by definition
            if self.contains_terminal(rhs):
                continue
            
            # If any production doesn't derive to lambda in the
            # rhs, then that rhs does not derive to lambda.
            if any(map(lambda prod: not self.derives_to_lambda(prod), rhs)):
                continue

            # If we've made it this far, then the rhs derives to lambda
            # and therefore the rule derives to lambda.
            return True

        return False        


#Read CFG from file
file_name = sys.argv[1]
cfg = CFG()
with open(file_name) as f:
    current_LHS = ""
    for line in f:
        line = line.strip("\n")

        tokens = line.split(" ")
        #check if this line is a production
        if tokens[1] == "->":
            current_LHS = tokens[0]
            RHS_tokens = tokens[2:]
            cfg.non_terminals.add(current_LHS)
        else:   #else line starts with alternation
            RHS_tokens = tokens[1:]

        alternation = []    #will contain symbols between each alternation
        for token in RHS_tokens:
            if token != "lambda" and token != "$" and token != "|":  #add the token to the cfg's respective set of terminals or non-terminals
                if token.islower():
                    cfg.terminals.add(token)
                else:
                    cfg.non_terminals.add(token)

            if token == "$":    #set the start symbol in the CFG to the non-terminal with $ in the production
                cfg.start_symbol = current_LHS
                alternation.append(token)
            elif token == "|":  #when you reach an alternation, add the last alternation list to the dict and then empty it
                cfg.production_rules[current_LHS].append(alternation)
                alternation = []
            else:
                alternation.append(token)

        cfg.production_rules[current_LHS].append(alternation)   #handles adding the last production to the dict


### print shit
print(f"Terminals: {', '.join(sorted(list(cfg.terminals)))}")
print(f"Non-terminals: {', '.join(sorted(list(cfg.non_terminals)))}\n")

counter = 1
for k,v in cfg.production_rules.items():
    for production in v:
        print(f"({counter})\t {k} -> {' '.join(production)}")
        counter += 1

print(f"\nGrammar Start Symbol or Goal: {cfg.start_symbol}")
print()
print(f"d2L(F) => {cfg.derives_to_lambda('F')}")