import json
import matplotlib.pyplot as plt
import matplotlib.image as mpimg
import os
import sys

from PIL import Image

def getBasePower(cardName):
	while (True):
		try:
			power = input(cardName + " power: ")
			if power != "":
				return int(power)
			else:
				return None
		except ValueError as e:
			print("Power must be numeric!")

def getCopies(cardName):
	while (True):
		try:
			power = input(cardName + " copies: ")
			if power != "":
				return int(power)
			else:
				return 1
		except ValueError as e:
			print("Copies must be numeric!")

def getInput(validInput, cardName, attribute):
	while (True):
		try:
			values = []
			inputValue = input(cardName + " " + attribute + ": ")
			values.extend([validInput[char] for char in inputValue])
			return values
		except KeyError as e:
			print(str(e) + ". Valid inputs are " + str(validInput))


def getFaction(cardName):
	# 'n' isn't used becaused it could match "nilfgaard",
	# "northernrealms" and "neutral"
	while True:
		try:
			types = {
				"e": "neutral",
				"m": "monsters",
				"i": "nilfgaard",
				"o": "northernrealms",
				"s": "scoiatael"
			}
			return getInput(types, cardName, "faction")[0]
		except IndexError as e:
			print(str(e) + ". Must supply faction.")

def getCombatTypes(cardName):
	types = {
		"m": "melee",
		"r": "ranged",
		"s": "siege",
		"w": "weather",
		"h": "horn",
		"l": "leader",
		"c": "scorch"
	}
	return getInput(types, cardName, "combat types")

def getAttributes(cardName):
	types = {
		"h": "hero",
		"c": "horn", #commander's horn
		"d": "decoy",
		"u": "muster",
		"o": "moraleBoost",
		"t": "tightBond",
		"e": "medic",
		"s": "spy",
		"x": "scorchMelee",
		"y": "scorchRanged",
		"z": "scorchSiege"
	}
	return getInput(types, cardName, "attributes")

def makeCard(cardName):
	card = {
		"id": cardName,
		"basePower": getBasePower(cardName),
		"combatTypes": getCombatTypes(cardName),
		"attributes": getAttributes(cardName),
		"faction": getFaction(cardName)
	}
	if "muster" in card["attributes"]:
		card["musterId"] = input("muster id: ")
	if "tightBond" in card["attributes"]:
		card["tightBondId"] = input("tightBond id: ")
	copies = getCopies(cardName)
	if copies != 1:
		card["copies"] = copies
	return card

def main():
	if len(sys.argv) < 3:
		print("Usage: generate_card_json.py <directory to card images> <output directory> <?card to start from>")
		return
	plt.ion()
	jumpToCard = sys.argv[3] if len(sys.argv) >= 4 else ""
	for fileName in os.listdir(sys.argv[1]):
		if fileName.endswith(".png"):
			cardName = fileName[:-(len(".png"))]
			if cardName < jumpToCard:
				continue
			img = mpimg.imread(os.path.join(sys.argv[1], fileName))
			plt.imshow(img)
			plt.pause(0.001)
			plt.show()
			#plt.draw()
			card = makeCard(cardName)
			with open(os.path.join(sys.argv[2], cardName + ".json"), "w") as output:
				output.write(json.dumps(card, indent=2, separators=(",", ": ")))

if __name__ == "__main__":
	main()