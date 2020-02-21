import sys
from collections import defaultdict

class CFG:

    def __init__(self):
        self.production_rules = defaultdict(list)   #{ LHS : list of lists }, where inner lists are possible alternations}
        self.terminals = set()
        self.non_terminals = set()
        self.start_symbol = ""


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
