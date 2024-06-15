{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/master";
  };

  outputs = { nixpkgs, systems, ... }:
    let
      system = "x86_64-linux";
      pkgs = nixpkgs.legacyPackages.${system}; 
    in
    {
      devShells.${system}.default = pkgs.mkShell {
        packages = [
          pkgs.scala_3
          pkgs.sbt
        ];
      };
    };
}
